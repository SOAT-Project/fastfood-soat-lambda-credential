# 🍔 FastFood SOAT - Lambda Credential Services

Este repositório contém dois microserviços serverless implementados em **Java 17** para a autenticação e autorização no sistema **FastFood SOAT**.  
Eles são implantados na **AWS Lambda** e expostos através do **API Gateway**.

---

## 📌 Arquitetura

O projeto é dividido em **duas Lambdas**:

1. **Auth Service (`auth-service`)**
    - Responsável por autenticar usuários.
    - Permite login via **CPF** ou como **guest/anônimo**.
    - Para usuários staff (funcionários), consulta permissões especiais no banco.
    - Gera um **JWT** assinado com `JWT_SECRET` que será usado nos endpoints protegidos.

2. **Middleware Service (`middleware-service`)**
    - Atua como um **gateway/middleware**.
    - Valida o JWT recebido no header (`Authorization: Bearer ...`).
    - Define regras de autorização:
        - **Staff** → acesso a todas as rotas.
        - **User** → acesso apenas às rotas de cliente.
        - **Guest** → acesso apenas às rotas de cliente.
    - Rejeita requisições com tokens inválidos ou expirados.

O **Auth Service** usa **PostgreSQL** via **RDS + RDS Proxy** para consultas de usuários/staff.

---

## ⚙️ Infraestrutura

- **AWS Lambda** → execução serverless do código Java.
- **Amazon RDS (PostgreSQL)** → persistência de usuários e staff.
- **RDS Proxy** → conexão otimizada e segura entre Lambda e banco.
- **Amazon S3** → armazenamento dos pacotes `.zip` das Lambdas.
- **IAM Roles** → permissões para as Lambdas (`lambda_role`).
- **API Gateway** → expõe os endpoints externos e roteia para as Lambdas.
- **Terraform** → IaC (Infrastructure as Code) para criar e atualizar as funções, buckets e roles.
- **GitHub Actions (CI/CD)** → build, empacotamento, upload e deploy automático.
- **OIDC (GitHub → AWS)** → autenticação segura entre GitHub Actions e AWS (sem precisar salvar `AWS_ACCESS_KEY_ID` e `AWS_SECRET_ACCESS_KEY`).

---

## 🛠️ Fluxo de CI/CD

O fluxo foi implementado com **GitHub Actions**, dividido em dois jobs principais:

1. **Build**
    - Compila o projeto com Gradle.
    - Gera o `.jar` usando **ShadowJar**.
    - Empacota em `.zip` para Lambda.
    - Faz **upload como artifact** (armazenado temporariamente pelo Actions).

2. **Deploy**
    - Baixa o `.zip` do job anterior.
    - Executa Terraform (`init`, `plan`, `apply`).
    - Atualiza a Lambda no AWS (via `aws_lambda_function`).
    - Usa variáveis de ambiente injetadas do **GitHub Secrets**:
        - `JWT_SECRET`
        - `DB_USER`
        - `DB_PASSWORD`
        - `DB_HOST`
        - `DB_NAME`

⚡ Não usamos **CodeDeploy** → o Terraform já faz o `UpdateFunctionCode` da Lambda. Isso simplifica o pipeline.

---

## 📂 Estrutura de diretórios

```bash
.
├── auth-service/               # Lambda de autenticação
│   ├── src/                    # Código fonte em Java
│   ├── build.gradle             # Build com Gradle
│   └── terraform/              # Infra da Lambda
│       ├── main.tf
│       ├── iam.tf
│       ├── lambda.tf
│       ├── provider.tf
│       ├── variables.tf
│       └── outputs.tf
│
├── middleware-service/         # Lambda middleware (JWT validation)
│   ├── src/
│   ├── build.gradle
│   └── terraform/
│       ├── main.tf
│       ├── iam.tf
│       ├── lambda.tf
│       ├── provider.tf
│       ├── variables.tf
│       └── outputs.tf
│
└── .github/workflows/          # Pipelines CI/CD
    ├── job-deploy-auth.yaml
    ├── job-deploy-middleware.yaml
    └── workflow-main-merge.yaml
```

---

## 🚀 Deploy manual (local)

1. **Build do projeto**
   ```bash
   ./gradlew :auth-service:clean :auth-service:shadowJar
   ./gradlew :middleware-service:clean :middleware-service:shadowJar
   ```

2. **Empacotar zip**
   ```bash
   zip -j auth-service/terraform/auth-service.zip auth-service/build/libs/*-all.jar
   zip -j middleware-service/terraform/middleware-service.zip middleware-service/build/libs/*-all.jar
   ```

3. **Terraform apply**
   ```bash
   cd auth-service/terraform
   terraform init
   terraform apply -auto-approve -var="jwt_secret=meu-segredo" -var="db_user=postgres" -var="db_password=123" -var="db_host=db.example.com" -var="db_name=fastfood"

   cd ../../middleware-service/terraform
   terraform init
   terraform apply -auto-approve -var="jwt_secret=meu-segredo" -var="db_user=postgres" -var="db_password=123" -var="db_host=db.example.com" -var="db_name=fastfood"
   ```

---

## 🔑 IAM Roles

- **auth-service-role**
    - Permite execução Lambda.
    - Acesso a RDS Proxy.
    - Logs no CloudWatch.

- **middleware-service-role**
    - Permite execução Lambda.
    - Apenas valida JWT → não acessa banco diretamente.
    - Logs no CloudWatch.

---

## ✅ Resumo

- **Auth Service** → gera tokens JWT para usuários, staff ou guests.
- **Middleware Service** → valida JWT e garante autorização por rota.
- **Terraform** → sobe e mantém Lambdas e S3.
- **GitHub Actions** → automatiza build, empacote e deploy com OIDC seguro.
- **Infra AWS** → Lambda + RDS Proxy + S3 + IAM + API Gateway.  
