# 🍔 FastFood SOAT - Lambda Credential Services

Este repositório contém dois microserviços serverless em **Java 17** para autenticação e autorização do sistema **FastFood SOAT**. Eles são implantados na **AWS Lambda** e expostos via **API Gateway**.

---

## 📌 Arquitetura

O projeto é dividido em **duas Lambdas**:

1. **Auth Service (`auth-service`)**
    - Autentica usuários (login via **CPF** ou guest/anônimo).
    - Autentica staff (login via **email**).
    - Gera **JWT** assinado com `JWT_SECRET` para endpoints protegidos.

2. **Middleware Service (`middleware-service`)**
    - Middleware/authorizer do API Gateway.
    - Valida JWT do header (`Authorization: Bearer ...`).
    - Regras de autorização:
        - **Staff** → acesso total.
        - **User/Guest** → acesso restrito a rotas de cliente.
    - Rejeita tokens inválidos/expirados.

---

## ⚙️ Infraestrutura

- **AWS Lambda**: execução serverless do Java.
- **Amazon RDS (PostgreSQL)**: persistência de usuários/staff.
- **Amazon S3**: armazenamento dos artefatos `.zip` das Lambdas.
- **IAM Roles**: permissões para as Lambdas.
- **API Gateway**: expõe endpoints e usa o middleware como authorizer.
- **Terraform**: IaC para criar/atualizar funções, buckets, roles.
- **GitHub Actions (CI/CD)**: build, empacotamento, upload e deploy automático.
- **OIDC (GitHub → AWS)**: autenticação segura do pipeline.

---

## 🛠️ CI/CD e Deploy

O deploy é automatizado via **GitHub Actions** e **Terraform**:

- **Build**: compila com Gradle, gera `.zip` (ShadowJar) e faz upload como artifact.
- **Deploy**: baixa o `.zip`, executa `terraform init/plan/apply` para atualizar a Lambda na AWS.
- **Secrets**: variáveis sensíveis (como `JWT_SECRET`, dados do banco e S3 para guardar os zip) são injetadas via GitHub Secrets.
- **OIDC**: autenticação do pipeline com AWS sem expor chaves.

### Principais Workflows
- `.github/workflows/job-deploy-auth.yaml`: build/deploy do Auth Service.
- `.github/workflows/job-deploy-middleware.yaml`: build/deploy do Middleware Service.
- `.github/workflows/workflow-main-merge.yaml`: orquestra deploys em merge na main/release.

---

## 🚀 Deploy manual (local)

1. **Build do projeto**
   ```bash
   ./gradlew :auth-service:clean :auth-service:buildZip
   ./gradlew :middleware-service:clean :middleware-service:buildZip
   ```

2. **Empacotar zip** (opcional, se não usar o buildZip)
   ```bash
   zip -j auth-service/terraform/auth-service.zip auth-service/build/libs/*-all.jar
   zip -j middleware-service/terraform/middleware-service.zip middleware-service/build/libs/*-all.jar
   ```

3. **Pré-requisitos AWS**
   - Crie o bucket S3: ex:`soat-credential-lambdas` (para armazenar os artefatos das Lambdas)
   - Crie a IAM Role ex:`lambda_role` com permissões:
     - `AWSLambdaBasicExecutionRole`
     - `AmazonRDSDataFullAccess`
     - (Exemplo: via console AWS, selecione as políticas ao criar a role)

4. **Terraform apply**
   ```bash
   cd auth-service/terraform
   terraform init
   terraform apply -auto-approve \
     -var="prefix=release" \
     -var="aws-region=sa-east-1" \
     -var="jwt_secret=meu-segredo" \
     -var="db_username=postgres" \
     -var="db_password=123" \
     -var="db_host=db.example.com" \
     -var="db_name=fastfood" \
     -var="s3_bucket_name=soat-credential-lambdas" \
     -var="lambda_role=arn:aws:iam::<account-id>:role/lambda_role"

   cd ../../middleware-service/terraform
   terraform init
   terraform apply -auto-approve \
     -var="prefix=release" \
     -var="aws-region=sa-east-1" \
     -var="jwt_secret=meu-segredo" \
     -var="s3_bucket_name=soat-credential-lambdas" \
     -var="lambda_role=arn:aws:iam::<account-id>:role/lambda_role"
   ```

---

## 🔑 Variáveis de ambiente (Terraform)

- `prefix`: ambiente (ex: `release`, `dev`, etc)
- `aws-region`: região AWS (ex: `sa-east-1`)
- `jwt_secret`: segredo do JWT
- `db_username`, `db_password`, `db_host`, `db_name`: dados do banco (apenas para Auth Service)
- `s3_bucket_name`: nome do bucket S3 para artefatos das Lambdas
- `lambda_role`: ARN da IAM Role para execução da Lambda

---

## ✅ Resumo

- **Auth Service** → gera tokens JWT para usuários, staff ou guest.
- **Middleware Service** → valida JWT e garante autorização por rota.
- **Terraform** → sobe e mantém Lambdas e S3.
- **GitHub Actions** → automatiza build, empacote e deploy com OIDC seguro.
- **Infra AWS** → Lambda + RDS Proxy + S3 + IAM + API Gateway.
