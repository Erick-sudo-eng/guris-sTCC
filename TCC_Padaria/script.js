// Dados iniciais
let usuarios = [];
let produtos = [];
let currentUser = null;
let produtoEditando = null;
let charts = {};

// Conta gerente fixa
const gerentePadrao = {
    username: 'gerente',
    password: '12345678',
    tipo: 'gerente'
};

// Inicializar dados
function inicializarDados() {
    // Carregar do localStorage
    const usuariosSalvos = localStorage.getItem('maf_usuarios');
    const produtosSalvos = localStorage.getItem('maf_produtos');
    
    if (usuariosSalvos) {
        try {
            usuarios = JSON.parse(usuariosSalvos);
            // Verificar se o gerente existe nos dados carregados
            const gerenteExiste = usuarios.find(u => u.username === 'gerente');
            if (!gerenteExiste) {
                usuarios.push(gerentePadrao);
            } else {
                // Garantir que a senha do gerente está correta
                const indexGerente = usuarios.findIndex(u => u.username === 'gerente');
                usuarios[indexGerente] = gerentePadrao;
            }
        } catch (e) {
            console.error('Erro ao carregar usuários:', e);
            usuarios = [gerentePadrao];
        }
    } else {
        usuarios = [gerentePadrao];
    }
    
    salvarUsuarios();
    
    if (produtosSalvos) {
        try {
            produtos = JSON.parse(produtosSalvos);
        } catch (e) {
            console.error('Erro ao carregar produtos:', e);
            produtos = [];
        }
    }
    
    if (produtos.length === 0) {
        // Produtos de exemplo com dados de venda
        produtos = [
            {
                id: 1,
                nome: 'Coxinha',
                quantidadeIdeal: 100,
                quantidadeAtual: 85,
                quantidadeVendidaMes: 150,
                dataFabricacao: '2026-06-30',
                validade: '2026-07-03',
                vendas: gerarVendasExemplo(150)
            },
            {
                id: 2,
                nome: 'Pastel de Carne',
                quantidadeIdeal: 80,
                quantidadeAtual: 45,
                quantidadeVendidaMes: 200,
                dataFabricacao: '2026-06-30',
                validade: '2026-07-01',
                vendas: gerarVendasExemplo(200)
            },
            {
                id: 3,
                nome: 'Empada de Frango',
                quantidadeIdeal: 60,
                quantidadeAtual: 15,
                quantidadeVendidaMes: 180,
                dataFabricacao: '2026-06-29',
                validade: '2026-07-01',
                vendas: gerarVendasExemplo(180)
            },
            {
                id: 4,
                nome: 'Bolinho de Queijo',
                quantidadeIdeal: 120,
                quantidadeAtual: 120,
                quantidadeVendidaMes: 90,
                dataFabricacao: '2026-06-30',
                validade: '2026-07-05',
                vendas: gerarVendasExemplo(90)
            }
        ];
        salvarProdutos();
    }
}

function gerarVendasExemplo(total) {
    const vendas = [];
    const diasNoMes = 30;
    const mediaDiaria = Math.floor(total / diasNoMes);
    
    for (let i = 0; i < diasNoMes; i++) {
        const variacao = Math.floor(Math.random() * (mediaDiaria * 0.5));
        vendas.push(Math.max(0, mediaDiaria + variacao));
    }
    return vendas;
}

function salvarUsuarios() {
    try {
        localStorage.setItem('maf_usuarios', JSON.stringify(usuarios));
    } catch (e) {
        console.error('Erro ao salvar usuários:', e);
    }
}

function salvarProdutos() {
    try {
        localStorage.setItem('maf_produtos', JSON.stringify(produtos));
    } catch (e) {
        console.error('Erro ao salvar produtos:', e);
    }
}

// Login
document.getElementById('loginForm').addEventListener('submit', function(e) {
    e.preventDefault();
    
    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value.trim();
    
    console.log('Tentando login:', username, password);
    console.log('Usuários disponíveis:', usuarios);
    
    const usuario = usuarios.find(u => u.username === username && u.password === password);
    
    if (usuario) {
        currentUser = usuario;
        document.getElementById('loginScreen').style.display = 'none';
        document.getElementById('mainScreen').style.display = 'block';
        document.getElementById('currentUser').textContent = `Bem-vindo, ${usuario.username} (${usuario.tipo})`;
        
        if (usuario.tipo === 'gerente') {
            document.getElementById('gerenciarUsuariosMenu').style.display = 'block';
        }
        
        document.getElementById('loginError').textContent = '';
        document.getElementById('username').value = '';
        document.getElementById('password').value = '';
        
        atualizarTabelaEstoque();
        atualizarTabelaUsuarios();
        inicializarGraficos();
        showSection('dashboard');
    } else {
        document.getElementById('loginError').textContent = 'Usuário ou senha incorretos!';
        console.log('Login falhou para:', username);
    }
});

function logout() {
    currentUser = null;
    document.getElementById('loginScreen').style.display = 'block';
    document.getElementById('mainScreen').style.display = 'none';
    document.getElementById('gerenciarUsuariosMenu').style.display = 'none';
    destruirGraficos();
    document.getElementById('loginError').textContent = '';
}

// Navegação
function showSection(section) {
    document.querySelectorAll('.section').forEach(s => s.classList.remove('active'));
    document.getElementById(section + 'Section').classList.add('active');
    
    document.querySelectorAll('.sidebar button').forEach(b => b.classList.remove('active'));
    
    const buttons = document.querySelectorAll('.sidebar button');
    buttons.forEach(button => {
        if (button.textContent.toLowerCase().includes(section)) {
            button.classList.add('active');
        }
    });
    
    if (section === 'dashboard') {
        setTimeout(inicializarGraficos, 100);
    } else if (section === 'estoque') {
        atualizarTabelaEstoque();
    } else if (section === 'usuarios') {
        atualizarTabelaUsuarios();
    } else if (section === 'adicionar' && !produtoEditando) {
        resetarFormulario();
    }
}

// Gráficos
function inicializarGraficos() {
    destruirGraficos();
    
    // Gráfico de Produtos Vendidos no Mês
    const ctxVendas = document.getElementById('chartVendasMes')?.getContext('2d');
    if (ctxVendas) {
        charts.vendasMes = new Chart(ctxVendas, {
            type: 'bar',
            data: {
                labels: produtos.map(p => p.nome),
                datasets: [{
                    label: 'Vendidos no Mês',
                    data: produtos.map(p => p.quantidadeVendidaMes || 0),
                    backgroundColor: 'rgba(30, 60, 114, 0.8)',
                    borderColor: 'rgba(30, 60, 114, 1)',
                    borderWidth: 2,
                    borderRadius: 5
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    legend: { display: false }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: { stepSize: 10 }
                    }
                }
            }
        });
    }
    
    // Gráfico de Produtos Vencidos
    const ctxVencidos = document.getElementById('chartVencidos')?.getContext('2d');
    if (ctxVencidos) {
        const hoje = new Date();
        const vencidos = produtos.filter(p => {
            const validade = new Date(p.validade);
            return validade < hoje;
        });
        
        const naoVencidos = produtos.filter(p => {
            const validade = new Date(p.validade);
            return validade >= hoje;
        });
        
        charts.vencidos = new Chart(ctxVencidos, {
            type: 'doughnut',
            data: {
                labels: ['Vencidos', 'Dentro do Prazo'],
                datasets: [{
                    data: [vencidos.length, naoVencidos.length],
                    backgroundColor: [
                        'rgba(220, 53, 69, 0.8)',
                        'rgba(40, 167, 69, 0.8)'
                    ],
                    borderColor: [
                        'rgba(220, 53, 69, 1)',
                        'rgba(40, 167, 69, 1)'
                    ],
                    borderWidth: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    legend: {
                        position: 'bottom'
                    }
                }
            }
        });
    }
    
    // Gráfico de Produtos com Mais Baixas no Mês
    const ctxBaixasMes = document.getElementById('chartBaixasMes')?.getContext('2d');
    if (ctxBaixasMes) {
        const produtosOrdenados = [...produtos].sort((a, b) => {
            const baixasA = calcularBaixasMes(a);
            const baixasB = calcularBaixasMes(b);
            return baixasB - baixasA;
        }).slice(0, 5);
        
        charts.baixasMes = new Chart(ctxBaixasMes, {
            type: 'bar',
            data: {
                labels: produtosOrdenados.map(p => p.nome),
                datasets: [{
                    label: 'Baixas no Mês',
                    data: produtosOrdenados.map(p => calcularBaixasMes(p)),
                    backgroundColor: 'rgba(255, 193, 7, 0.8)',
                    borderColor: 'rgba(255, 193, 7, 1)',
                    borderWidth: 2,
                    borderRadius: 5
                }]
            },
            options: {
                indexAxis: 'y',
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    legend: { display: false }
                },
                scales: {
                    x: {
                        beginAtZero: true,
                        ticks: { stepSize: 5 }
                    }
                }
            }
        });
    }
    
    // Gráfico de Produtos com Mais Baixas na Semana
    const ctxBaixasSemana = document.getElementById('chartBaixasSemana')?.getContext('2d');
    if (ctxBaixasSemana) {
        const produtosOrdenadosSemana = [...produtos].sort((a, b) => {
            const baixasA = calcularBaixasSemana(a);
            const baixasB = calcularBaixasSemana(b);
            return baixasB - baixasA;
        }).slice(0, 5);
        
        charts.baixasSemana = new Chart(ctxBaixasSemana, {
            type: 'bar',
            data: {
                labels: produtosOrdenadosSemana.map(p => p.nome),
                datasets: [{
                    label: 'Baixas na Semana',
                    data: produtosOrdenadosSemana.map(p => calcularBaixasSemana(p)),
                    backgroundColor: 'rgba(23, 162, 184, 0.8)',
                    borderColor: 'rgba(23, 162, 184, 1)',
                    borderWidth: 2,
                    borderRadius: 5
                }]
            },
            options: {
                indexAxis: 'y',
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    legend: { display: false }
                },
                scales: {
                    x: {
                        beginAtZero: true,
                        ticks: { stepSize: 5 }
                    }
                }
            }
        });
    }
}

function calcularBaixasMes(produto) {
    if (!produto.vendas) return 0;
    return produto.vendas.reduce((a, b) => a + b, 0);
}

function calcularBaixasSemana(produto) {
    if (!produto.vendas) return 0;
    return produto.vendas.slice(-7).reduce((a, b) => a + b, 0);
}

function destruirGraficos() {
    Object.values(charts).forEach(chart => {
        if (chart) chart.destroy();
    });
    charts = {};
}

// Gerenciamento de Produtos
document.getElementById('produtoForm').addEventListener('submit', function(e) {
    e.preventDefault();
    
    const produtoData = {
        nome: document.getElementById('produtoNome').value,
        quantidadeIdeal: parseInt(document.getElementById('quantidadeIdeal').value),
        quantidadeAtual: parseInt(document.getElementById('quantidadeAtual').value),
        quantidadeVendidaMes: parseInt(document.getElementById('quantidadeVendidaMes').value) || 0,
        dataFabricacao: document.getElementById('dataFabricacao').value,
        validade: document.getElementById('validade').value
    };
    
    if (produtoEditando) {
        // Editar produto existente
        const index = produtos.findIndex(p => p.id === produtoEditando);
        if (index !== -1) {
            produtos[index] = { ...produtos[index], ...produtoData };
            alert('Produto atualizado com sucesso!');
        }
    } else {
        // Adicionar novo produto
        const novoProduto = {
            id: Date.now(),
            ...produtoData,
            vendas: Array(30).fill(0)
        };
        produtos.push(novoProduto);
        alert('Produto adicionado com sucesso!');
    }
    
    salvarProdutos();
    resetarFormulario();
    atualizarTabelaEstoque();
    inicializarGraficos();
});

function editarProduto(id) {
    const produto = produtos.find(p => p.id === id);
    if (produto) {
        produtoEditando = id;
        document.getElementById('formTitle').textContent = 'Editar Produto';
        document.getElementById('produtoId').value = produto.id;
        document.getElementById('produtoNome').value = produto.nome;
        document.getElementById('quantidadeIdeal').value = produto.quantidadeIdeal;
        document.getElementById('quantidadeAtual').value = produto.quantidadeAtual;
        document.getElementById('quantidadeVendidaMes').value = produto.quantidadeVendidaMes || 0;
        document.getElementById('dataFabricacao').value = produto.dataFabricacao;
        document.getElementById('validade').value = produto.validade;
        document.getElementById('btnCancelar').style.display = 'block';
        
        showSection('adicionar');
    }
}

function cancelarEdicao() {
    resetarFormulario();
}

function resetarFormulario() {
    produtoEditando = null;
    document.getElementById('formTitle').textContent = 'Adicionar Novo Produto';
    document.getElementById('produtoForm').reset();
    document.getElementById('produtoId').value = '';
    document.getElementById('btnCancelar').style.display = 'none';
}

function deletarProduto(id) {
    if (confirm('Tem certeza que deseja excluir este produto?')) {
        produtos = produtos.filter(p => p.id !== id);
        salvarProdutos();
        atualizarTabelaEstoque();
        inicializarGraficos();
    }
}

function abrirModalVenda(id) {
    const produto = produtos.find(p => p.id === id);
    if (produto) {
        document.getElementById('produtoVendaNome').textContent = `Produto: ${produto.nome}`;
        document.getElementById('vendaForm').dataset.produtoId = id;
        document.getElementById('vendaModal').style.display = 'block';
        document.getElementById('quantidadeVenda').value = '';
        document.getElementById('quantidadeVenda').focus();
    }
}

function fecharModalVenda() {
    document.getElementById('vendaModal').style.display = 'none';
}

// Fechar modal clicando fora
window.onclick = function(event) {
    const modal = document.getElementById('vendaModal');
    if (event.target === modal) {
        fecharModalVenda();
    }
}

document.getElementById('vendaForm').addEventListener('submit', function(e) {
    e.preventDefault();
    
    const produtoId = parseInt(this.dataset.produtoId);
    const quantidade = parseInt(document.getElementById('quantidadeVenda').value);
    
    const produto = produtos.find(p => p.id === produtoId);
    if (produto) {
        if (quantidade > produto.quantidadeAtual) {
            alert('Quantidade insuficiente em estoque!');
            return;
        }
        
        produto.quantidadeAtual -= quantidade;
        produto.quantidadeVendidaMes = (produto.quantidadeVendidaMes || 0) + quantidade;
        
        // Registrar venda no histórico
        if (!produto.vendas) {
            produto.vendas = Array(30).fill(0);
        }
        produto.vendas[produto.vendas.length - 1] += quantidade;
        
        salvarProdutos();
        fecharModalVenda();
        atualizarTabelaEstoque();
        inicializarGraficos();
        alert(`Venda registrada: ${quantidade} unidades de ${produto.nome}`);
    }
});

function getStatus(produto) {
    const hoje = new Date();
    const validade = new Date(produto.validade);
    const diasRestantes = Math.ceil((validade - hoje) / (1000 * 60 * 60 * 24));
    
    if (diasRestantes < 0) {
        return { texto: 'VENCIDO', classe: 'status-expired' };
    } else if (produto.quantidadeAtual === 0) {
        return { texto: 'ESGOTADO', classe: 'status-critical' };
    } else if (produto.quantidadeAtual < produto.quantidadeIdeal * 0.3) {
        return { texto: 'CRÍTICO', classe: 'status-critical' };
    } else if (produto.quantidadeAtual < produto.quantidadeIdeal * 0.5) {
        return { texto: 'BAIXO', classe: 'status-low' };
    } else {
        return { texto: 'OK', classe: 'status-ok' };
    }
}

function atualizarTabelaEstoque() {
    const tbody = document.getElementById('estoqueBody');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    produtos.forEach(produto => {
        const status = getStatus(produto);
        const row = tbody.insertRow();
        
        row.innerHTML = `
            <td>${produto.nome}</td>
            <td>${produto.quantidadeIdeal}</td>
            <td>${produto.quantidadeAtual}</td>
            <td>${formatarData(produto.dataFabricacao)}</td>
            <td>${formatarData(produto.validade)}</td>
            <td>${produto.quantidadeVendidaMes || 0}</td>
            <td><span class="status-badge ${status.classe}">${status.texto}</span></td>
            <td>
                <button onclick="abrirModalVenda(${produto.id})" class="btn-action btn-venda">💰 Vender</button>
                <button onclick="editarProduto(${produto.id})" class="btn-action btn-edit">✏️ Editar</button>
                <button onclick="deletarProduto(${produto.id})" class="btn-action btn-delete">🗑️ Excluir</button>
            </td>
        `;
    });
}

// Gerenciamento de Usuários
document.getElementById('usuarioForm').addEventListener('submit', function(e) {
    e.preventDefault();
    
    if (currentUser.tipo !== 'gerente') {
        alert('Apenas o gerente pode criar usuários!');
        return;
    }
    
    const username = document.getElementById('novoUsuario').value.trim();
    const password = document.getElementById('novaSenha').value;
    const tipo = document.getElementById('tipoUsuario').value;
    
    // Verificar se usuário já existe
    if (usuarios.find(u => u.username === username)) {
        alert('Este usuário já existe!');
        return;
    }
    
    usuarios.push({ username, password, tipo });
    salvarUsuarios();
    
    // Limpar formulário
    this.reset();
    
    alert('Usuário criado com sucesso!');
    atualizarTabelaUsuarios();
});

function atualizarTabelaUsuarios() {
    const tbody = document.getElementById('usuariosBody');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    usuarios.forEach(usuario => {
        const row = tbody.insertRow();
        const isGerente = usuario.username === 'gerente';
        
        row.innerHTML = `
            <td>${usuario.username}</td>
            <td>${usuario.tipo}</td>
            <td>
                ${!isGerente ? 
                    `<button onclick="deletarUsuario('${usuario.username}')" class="btn-action btn-delete">🗑️ Excluir</button>` 
                    : '<span style="color: #999;">🔒 Protegido</span>'}
            </td>
        `;
    });
}

function deletarUsuario(username) {
    if (username === 'gerente') {
        alert('Não é possível excluir a conta do gerente!');
        return;
    }
    
    if (currentUser.username === username) {
        alert('Você não pode excluir sua própria conta!');
        return;
    }
    
    if (confirm(`Tem certeza que deseja excluir o usuário ${username}?`)) {
        usuarios = usuarios.filter(u => u.username !== username);
        salvarUsuarios();
        atualizarTabelaUsuarios();
        alert('Usuário excluído com sucesso!');
    }
}

// Funções auxiliares
function formatarData(data) {
    if (!data) return 'N/A';
    const [ano, mes, dia] = data.split('-');
    return `${dia}/${mes}/${ano}`;
}

// Exportação
function exportToPDF() {
    const { jsPDF } = window.jspdf;
    const doc = new jsPDF();
    
    // Título
    doc.setFontSize(20);
    doc.text('M.A.F Salgados - Controle de Estoque', 14, 22);
    
    // Data
    doc.setFontSize(10);
    doc.text(`Data: ${new Date().toLocaleDateString('pt-BR')}`, 14, 30);
    
    // Tabela
    const headers = [['Produto', 'Qtd. Ideal', 'Qtd. Atual', 'Fabricação', 'Validade', 'Vendidos Mês', 'Status']];
    const data = produtos.map(p => [
        p.nome,
        p.quantidadeIdeal,
        p.quantidadeAtual,
        formatarData(p.dataFabricacao),
        formatarData(p.validade),
        p.quantidadeVendidaMes || 0,
        getStatus(p).texto
    ]);
    
    doc.autoTable({
        head: headers,
        body: data,
        startY: 35,
        styles: {
            fontSize: 8,
            cellPadding: 3,
        },
        headStyles: {
            fillColor: [30, 60, 114],
            textColor: 255,
            fontStyle: 'bold',
        },
        alternateRowStyles: {
            fillColor: [240, 244, 248],
        }
    });
    
    doc.save('estoque_maf_salgados.pdf');
}

function exportToExcel() {
    const data = produtos.map(p => ({
        'Produto': p.nome,
        'Quantidade Ideal': p.quantidadeIdeal,
        'Quantidade Atual': p.quantidadeAtual,
        'Vendidos no Mês': p.quantidadeVendidaMes || 0,
        'Data Fabricação': formatarData(p.dataFabricacao),
        'Validade': formatarData(p.validade),
        'Status': getStatus(p).texto
    }));
    
    const ws = XLSX.utils.json_to_sheet(data);
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Estoque');
    
    XLSX.writeFile(wb, 'estoque_maf_salgados.xlsx');
}

// Inicializar quando a página carregar
document.addEventListener('DOMContentLoaded', function() {
    inicializarDados();
    console.log('Sistema inicializado. Gerente configurado:', gerentePadrao);
});