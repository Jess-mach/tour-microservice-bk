# TourApp Backend

Sistema de gerenciamento de excursões desenvolvido em Spring Boot.

## 🚀 Funcionalidades

### Para Clientes
- ✅ Cadastro e autenticação
- ✅ Visualização de excursões públicas
- ✅ Inscrição em excursões
- ✅ Pagamento via PIX e Cartão de Crédito
- ✅ Área do cliente com histórico
- ✅ Notificações push e email

### Para Organizadores
- ✅ Cadastro e autenticação
- ✅ CRUD de excursões
- ✅ Upload de imagens
- ✅ Gestão de inscritos
- ✅ Dashboard com métricas
- ✅ Sistema de notificações para clientes
- ✅ Controle de pagamentos

## 🛠 Stack Tecnológica

- **Framework**: Spring Boot 3.2
- **Linguagem**: Java 17
- **Banco de Dados**: PostgreSQL
- **Autenticação**: JWT
- **Pagamentos**: Mercado Pago SDK
- **Upload de Imagens**: Cloudinary
- **Notificações**: Firebase Cloud Messaging
- **Email**: Spring Mail + SendGrid
- **Migrations**: Flyway
- **Build**: Maven

## 🏗 Estrutura do Projeto


```
tourapp-backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── br/
│   │   │       └── com/
│   │   │           └── tourapp/
│   │   │               ├── TourappApplication.java
│   │   │               ├── config/
│   │   │               │   ├── SecurityConfig.java
│   │   │               │   ├── CloudinaryConfig.java
│   │   │               │   ├── MercadoPagoConfig.java
│   │   │               │   ├── FirebaseConfig.java
│   │   │               │   └── CorsConfig.java
│   │   │               ├── controller/
│   │   │               │   ├── AuthController.java
│   │   │               │   ├── OrganizadorController.java
│   │   │               │   ├── ClienteController.java
│   │   │               │   ├── ExcursaoController.java
│   │   │               │   ├── InscricaoController.java
│   │   │               │   ├── PagamentoController.java
│   │   │               │   ├── NotificacaoController.java
│   │   │               │   └── PublicController.java
│   │   │               ├── dto/
│   │   │               │   ├── request/
│   │   │               │   │   ├── LoginRequest.java
│   │   │               │   │   ├── CadastroOrganizadorRequest.java
│   │   │               │   │   ├── CadastroClienteRequest.java
│   │   │               │   │   ├── ExcursaoRequest.java
│   │   │               │   │   ├── InscricaoRequest.java
│   │   │               │   │   ├── PagamentoPixRequest.java
│   │   │               │   │   ├── PagamentoCartaoRequest.java
│   │   │               │   │   └── NotificacaoRequest.java
│   │   │               │   └── response/
│   │   │               │       ├── AuthResponse.java
│   │   │               │       ├── OrganizadorResponse.java
│   │   │               │       ├── ClienteResponse.java
│   │   │               │       ├── ExcursaoResponse.java
│   │   │               │       ├── ExcursaoPublicaResponse.java
│   │   │               │       ├── InscricaoResponse.java
│   │   │               │       ├── DashboardResponse.java
│   │   │               │       ├── PagamentoResponse.java
│   │   │               │       └── NotificacaoResponse.java
│   │   │               ├── entity/
│   │   │               │   ├── Cliente.java
│   │   │               │   ├── Organizador.java
│   │   │               │   ├── Excursao.java
│   │   │               │   ├── Inscricao.java
│   │   │               │   ├── Pagamento.java
│   │   │               │   ├── Notificacao.java
│   │   │               │   └── BaseEntity.java
│   │   │               ├── enums/
│   │   │               │   ├── StatusOrganizador.java
│   │   │               │   ├── StatusExcursao.java
│   │   │               │   ├── StatusPagamento.java
│   │   │               │   ├── MetodoPagamento.java
│   │   │               │   ├── TipoNotificacao.java
│   │   │               │   └── TipoUsuario.java
│   │   │               ├── exception/
│   │   │               │   ├── GlobalExceptionHandler.java
│   │   │               │   ├── BusinessException.java
│   │   │               │   ├── NotFoundException.java
│   │   │               │   ├── UnauthorizedException.java
│   │   │               │   └── ValidationException.java
│   │   │               ├── repository/
│   │   │               │   ├── ClienteRepository.java
│   │   │               │   ├── OrganizadorRepository.java
│   │   │               │   ├── ExcursaoRepository.java
│   │   │               │   ├── InscricaoRepository.java
│   │   │               │   ├── PagamentoRepository.java
│   │   │               │   └── NotificacaoRepository.java
│   │   │               ├── security/
│   │   │               │   ├── JwtUtil.java
│   │   │               │   ├── JwtAuthenticationFilter.java
│   │   │               │   ├── CustomUserDetailsService.java
│   │   │               │   └── SecurityUser.java
│   │   │               ├── service/
│   │   │               │   ├── AuthService.java
│   │   │               │   ├── OrganizadorService.java
│   │   │               │   ├── ClienteService.java
│   │   │               │   ├── ExcursaoService.java
│   │   │               │   ├── InscricaoService.java
│   │   │               │   ├── PagamentoService.java
│   │   │               │   ├── NotificacaoService.java
│   │   │               │   ├── EmailService.java
│   │   │               │   ├── CloudinaryService.java
│   │   │               │   └── FirebaseService.java
│   │   │               └── util/
│   │   │                   ├── ValidationUtil.java
│   │   │                   ├── DateUtil.java
│   │   │                   ├── StringUtil.java
│   │   │                   └── Constants.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       ├── db/
│   │       │   └── migration/
│   │       │       ├── V1__create_cliente_table.sql
│   │       │       ├── V2__create_organizador_table.sql
│   │       │       ├── V3__create_excursao_table.sql
│   │       │       ├── V4__create_inscricao_table.sql
│   │       │       ├── V5__create_pagamento_table.sql
│   │       │       ├── V6__create_notificacao_table.sql
│   │       │       └── V7__insert_initial_data.sql
│   │       ├── static/
│   │       └── templates/
│   │           ├── email/
│   │           │   ├── confirmacao-inscricao.html
│   │           │   ├── confirmacao-pagamento.html
│   │           │   └── lembrete-excursao.html
│   │           └── firebase-service-account.json
│   └── test/
│       └── java/
│           └── br/
│               └── com/
│                   └── tourapp/
│                       ├── TourappApplicationTests.java
│                       ├── controller/
│                       ├── service/
│                       └── repository/
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── README.md
└── .gitignore

```

## ⚙️ Configuração

### 1. Pré-requisitos
- Java 21+
- PostgreSQL 12+
- Maven 3.9+

### 2. Variáveis de Ambiente

Crie um arquivo `.env` ou configure as seguintes variáveis:

```bash
# Database
DB_URL=jdbc:postgresql://localhost:5432/tourapp
DB_USER=tourapp
DB_PASSWORD=tourapp123

# JWT
JWT_SECRET=tourapp-super-secret-key-change-in-production
JWT_EXPIRATION=86400000

# Mercado Pago
MERCADOPAGO_ACCESS_TOKEN=seu_access_token
MERCADOPAGO_PUBLIC_KEY=sua_public_key
MERCADOPAGO_WEBHOOK_SECRET=seu_webhook_secret

# Cloudinary
CLOUDINARY_CLOUD_NAME=seu_cloud_name
CLOUDINARY_API_KEY=sua_api_key
CLOUDINARY_API_SECRET=seu_api_secret

# Firebase
FIREBASE_SERVICE_ACCOUNT_PATH=classpath:firebase-service-account.json

# Email
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=seu_email@gmail.com
MAIL_PASSWORD=sua_senha_app

# Frontend
FRONTEND_BASE_URL=http://localhost:3000
CORS_ORIGINS=http://localhost:3000,https://tourapp.vercel.app
```

### 3. Configuração do Banco

```sql
-- Criar banco
CREATE DATABASE tourapp;

-- Criar usuário
CREATE USER tourapp WITH PASSWORD 'tourapp123';
GRANT ALL PRIVILEGES ON DATABASE tourapp TO tourapp;
```

### 4. Executar Localmente

```bash
# Clonar o repositório
git clone <repo-url>
cd tourapp-backend

# Instalar dependências
mvn clean install

# Executar aplicação
mvn spring-boot:run
```

A aplicação estará disponível em `http://localhost:8080/api`

## 🚀 Deploy no Railway

### 1. Conectar Repositório
1. Acesse [Railway](https://railway.app)
2. Conecte seu repositório GitHub
3. Selecione o projeto

### 2. Configurar Variáveis
No Railway, configure todas as variáveis de ambiente listadas acima.

### 3. Configurar Banco PostgreSQL
1. Adicione o plugin PostgreSQL no Railway
2. Use as credenciais geradas nas variáveis `DB_*`

### 4. Deploy Automático
O Railway fará deploy automático a cada push na branch main.

## 📚 Documentação da API

### Endpoints Públicos

#### Autenticação
```http
POST /api/auth/login
POST /api/auth/cadastro/cliente
POST /api/auth/cadastro/organizador
POST /api/auth/refresh
```

#### Excursões Públicas
```http
GET /api/public/excursoes/{id}
POST /api/public/excursoes/{id}/inscricoes
```

### Endpoints do Cliente (Requer token)

```http
GET /api/cliente/perfil
PUT /api/cliente/perfil
GET /api/cliente/inscricoes
GET /api/cliente/inscricoes/{id}
PUT /api/cliente/notificacoes/push-token
PUT /api/cliente/notificacoes/configuracoes
```

### Endpoints do Organizador (Requer token)

#### Excursões
```http
GET /api/organizador/excursoes
POST /api/organizador/excursoes
GET /api/organizador/excursoes/{id}
PUT /api/organizador/excursoes/{id}
PATCH /api/organizador/excursoes/{id}/status
DELETE /api/organizador/excursoes/{id}
```

#### Dashboard e Gestão
```http
GET /api/organizador/perfil
PUT /api/organizador/perfil
GET /api/organizador/dashboard
GET /api/organizador/inscricoes
GET /api/organizador/excursoes/{id}/inscricoes
```

#### Notificações
```http
POST /api/organizador/notificacoes
POST /api/organizador/notificacoes/{id}/enviar
GET /api/organizador/notificacoes
GET /api/organizador/notificacoes/clientes/{excursaoId}
```

### Endpoints de Pagamento

```http
POST /api/pagamentos/pix
POST /api/pagamentos/cartao
```

## 🔧 Configurações Avançadas

### Mercado Pago Webhooks

Configure o webhook no Mercado Pago apontando para:
```
https://seu-app.railway.app/api/webhook/mercadopago
```

### Firebase Cloud Messaging

1. Crie um projeto no Firebase Console
2. Baixe o arquivo `firebase-service-account.json`
3. Adicione no diretório `src/main/resources/`

### Cloudinary

1. Crie uma conta no Cloudinary
2. Configure as credenciais nas variáveis de ambiente
3. O upload será automático nos endpoints de criação/edição de excursões

## 🧪 Testes

```bash
# Executar todos os testes
mvn test

# Executar testes específicos
mvn test -Dtest=AuthServiceTest

# Executar com cobertura
mvn jacoco:report
```

## 📊 Monitoramento

### Health Check
```http
GET /api/health
GET /api/actuator/health
```

### Métricas
```http
GET /api/actuator/metrics
```

## 🐳 Docker

```bash
# Build da imagem
docker build -t tourapp-backend .

# Executar container
docker run -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://host:5432/tourapp \
  -e DB_USER=tourapp \
  -e DB_PASSWORD=tourapp123 \
  tourapp-backend
```

### Docker Compose

```yaml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - DB_URL=jdbc:postgresql://db:5432/tourapp
      - DB_USER=tourapp
      - DB_PASSWORD=tourapp123
    depends_on:
      - db
  
  db:
    image: postgres:15
    environment:
      POSTGRES_DB: tourapp
      POSTGRES_USER: tourapp
      POSTGRES_PASSWORD: tourapp123
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

## 🚨 Troubleshooting

### Problemas Comuns

1. **Erro de conexão com banco**
    - Verifique se o PostgreSQL está rodando
    - Confirme as credenciais nas variáveis de ambiente

2. **JWT Token inválido**
    - Verifique se o `JWT_SECRET` está configurado
    - Confirme se o token não expirou

3. **Upload de imagens falhando**
    - Verifique as credenciais do Cloudinary
    - Confirme o tamanho do arquivo (max 10MB)

4. **Pagamentos não funcionando**
    - Verifique as credenciais do Mercado Pago
    - Confirme se o webhook está configurado

### Logs

```bash
# Ver logs no Railway
railway logs

# Logs locais
tail -f logs/tourapp.log
```

## 📈 Próximos Passos

### Funcionalidades Futuras
- [ ] Sistema de reviews/avaliações
- [ ] Chat entre organizador e clientes
- [ ] Marketplace público de excursões
- [ ] Sistema de afiliados
- [ ] Relatórios avançados
- [ ] App mobile nativo
- [ ] Integração com WhatsApp Business

### Melhorias Técnicas
- [ ] Cache com Redis
- [ ] Testes de integração completos
- [ ] CI/CD com GitHub Actions
- [ ] Monitoramento com Prometheus
- [ ] Documentação OpenAPI/Swagger

## 🤝 Contribuição

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📝 Licença

Este projeto está sob a licença MIT. Veja o arquivo `LICENSE` para mais detalhes.

## 📞 Contato

Para dúvidas sobre o projeto, entre em contato através dos issues do GitHub.


docker run --name postgres-db -e POSTGRES_PASSWORD=123456 -p 5432:5432 -d postgres:15