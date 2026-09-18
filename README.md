# EventHub

<p align="center">
  <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/Spring%20Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white" alt="Spring Security"/>
  <img src="https://img.shields.io/badge/Spring%20Data%20JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="Spring Data JPA"/>
  <img src="https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white" alt="JWT"/>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL"/>
  <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="Docker"/>
  <img src="https://img.shields.io/badge/Amazon%20ECS-FF9900?style=for-the-badge&logo=amazonecs&logoColor=white" alt="Amazon ECS"/>
  <img src="https://img.shields.io/badge/AWS%20Fargate-FF9900?style=for-the-badge&logo=awsfargate&logoColor=white" alt="AWS Fargate"/>
  <img src="https://img.shields.io/badge/Amazon%20S3-569A31?style=for-the-badge&logo=amazons3&logoColor=white" alt="Amazon S3"/>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Amazon%20RDS-527FFF?style=for-the-badge&logo=amazonrds&logoColor=white" alt="Amazon RDS"/>
  <img src="https://img.shields.io/badge/Amazon%20ECR-FF9900?style=for-the-badge&logo=amazonecr&logoColor=white" alt="Amazon ECR"/>
  <img src="https://img.shields.io/badge/AWS%20IAM-232F3E?style=for-the-badge&logo=amazoniam&logoColor=white" alt="AWS IAM"/>
  <img src="https://img.shields.io/badge/Secrets%20Manager-FF9900?style=for-the-badge&logo=amazonaws&logoColor=white" alt="AWS Secrets Manager"/>
  <img src="https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black" alt="Swagger"/>
</p>


<p align="center">
  REST API for event management and registration, built with Java and Spring Boot,
  featuring JWT authentication, role-based authorization, automated testing,
  Docker containerization and AWS deployment.
</p>



## 1 - About

  <strong>EventHub</strong> is a REST API developed with Java and Spring Boot for
  managing events and user registrations.The project was developed with a focus on backend development, software architecture,
  security, automated testing, containerization and cloud infrastructure, and this application allows organizers to create and manage events, while authenticated users can register for available events. The project was developed incrementally, starting as a local Spring Boot application and evolving into a containerized application deployed on AWS using managed cloud services.


## 2 - Features

### I - User Management

<ul>
  <li>User registration</li>
  <li>User authentication</li>
  <li>JWT-based authentication</li>
  <li>Role-based authorization</li>
</ul>

### II - Event Management

<ul>
  <li>Create events</li>
  <li>List events</li>
  <li>Find events by ID</li>
  <li>Update events</li>
  <li>Publish events</li>
  <li>Delete events</li>
  <li>Organizer ownership validation</li>
  <li>Administrator management permissions</li>
</ul>

### III - Event Registration

<ul>
  <li>Register users for events</li>
  <li>Cancel registrations</li>
  <li>List the authenticated user's registrations</li>
  <li>List registrations for an event</li>
  <li>Access control for participant lists</li>
</ul>

### IV - Event Images

<ul>
  <li>Upload event images</li>
  <li>Private image storage using Amazon S3</li>
  <li>S3 object key persistence</li>
  <li>Pre-signed URL generation</li>
  <li>Temporary image access</li>
  <li>Image replacement when updating an event</li>
</ul>



## 3 - Authentication Flow

<p>
  EventHub uses <strong>JWT</strong> for stateless authentication.
  After successfully logging in, the user receives a JSON Web Token that must
  be sent in the <code>Authorization</code> header of authenticated requests.
</p>

<img width="1246" height="163" alt="image" src="https://github.com/user-attachments/assets/837a1bd9-eae8-4ed1-a500-2cd2d7d6d1ac" />

### I - Authentication Endpoints

<table>
  <tr>
    <th>Method</th>
    <th>Endpoint</th>
    <th>Access</th>
  </tr>
  <tr>
    <td>POST</td>
    <td><code>/api/v1/auth/register</code></td>
    <td>Public</td>
  </tr>
  <tr>
    <td>POST</td>
    <td><code>/api/v1/auth/login</code></td>
    <td>Public</td>
  </tr>
</table>

## 4 - Security

<p>
  Security is implemented using <strong>Spring Security</strong> with JWT-based
  authentication and role-based authorization.
</p>

### I - Roles

<table>
  <tr>
    <th>Role</th>
    <th>Description</th>
  </tr>
  <tr>
    <td><code>USER</code></td>
    <td>Authenticated user who can register for events.</td>
  </tr>
  <tr>
    <td><code>ORGANIZER</code></td>
    <td>User authorized to create and manage events.</td>
  </tr>
  <tr>
    <td><code>ADMIN</code></td>
    <td>User with administrative permissions over events.</td>
  </tr>
</table>

### II - Authorization

<p>
  The API validates both the user's role and, when necessary, resource ownership.
  Organizers can manage their own events, while administrators have management
  permissions regardless of the event organizer.
</p>

<img width="1242" height="289" alt="image" src="https://github.com/user-attachments/assets/3bef76da-6000-4e4e-8eea-7e00ae83cd24" />

### III - HTTP Security Responses

<ul>
  <li><code>401 Unauthorized</code> — the request is not authenticated.</li>
  <li><code>403 Forbidden</code> — the authenticated user does not have permission.</li>
</ul>

<p>
  The application uses a stateless session policy and disables CSRF because the
  API uses JWT-based authentication instead of browser sessions.
</p>



## 5 - API

The API uses the following base path:

<pre><code>/api/v1</code></pre>

### I - Authentication

<table>
  <tr>
    <th>Method</th>
    <th>Endpoint</th>
    <th>Access</th>
  </tr>
  <tr>
    <td>POST</td>
    <td><code>/api/v1/auth/register</code></td>
    <td>Public</td>
  </tr>
  <tr>
    <td>POST</td>
    <td><code>/api/v1/auth/login</code></td>
    <td>Public</td>
  </tr>
</table>

### II - Events

<table>
  <tr>
    <th>Method</th>
    <th>Endpoint</th>
    <th>Access</th>
  </tr>
  <tr>
    <td>GET</td>
    <td><code>/api/v1/events</code></td>
    <td>Public</td>
  </tr>
  <tr>
    <td>GET</td>
    <td><code>/api/v1/events/{id}</code></td>
    <td>Public</td>
  </tr>
  <tr>
    <td>POST</td>
    <td><code>/api/v1/events</code></td>
    <td>ORGANIZER / ADMIN</td>
  </tr>
  <tr>
    <td>PUT</td>
    <td><code>/api/v1/events/{id}</code></td>
    <td>ORGANIZER / ADMIN</td>
  </tr>
  <tr>
    <td>PATCH</td>
    <td><code>/api/v1/events/{id}</code></td>
    <td>ORGANIZER / ADMIN</td>
  </tr>
  <tr>
    <td>DELETE</td>
    <td><code>/api/v1/events/{id}</code></td>
    <td>ORGANIZER / ADMIN</td>
  </tr>
</table>

### III - Registrations

<table>
  <tr>
    <th>Method</th>
    <th>Endpoint</th>
    <th>Access</th>
  </tr>
  <tr>
    <td>POST</td>
    <td><code>/api/v1/events/{eventId}/registrations</code></td>
    <td>Authenticated</td>
  </tr>
  <tr>
    <td>GET</td>
    <td><code>/api/v1/users/me/registrations</code></td>
    <td>Authenticated</td>
  </tr>
  <tr>
    <td>GET</td>
    <td><code>/api/v1/events/{eventId}/registrations</code></td>
    <td>ORGANIZER / ADMIN</td>
  </tr>
  <tr>
    <td>DELETE</td>
    <td><code>/api/v1/events/{eventId}/registrations</code></td>
    <td>Authenticated</td>
  </tr>
</table>

### IV - Multipart Requests

Event creation and updates support multipart requests when an image is provided.

<pre><code>Content-Type: multipart/form-data

event:
{
    "title": "Example Event",
    "description": "Event description",
    "date": "2026-10-01T19:00:00",
    "location": "São Paulo",
    "capacity": 100
}

image:
[event image]
</code></pre>



## 6 - Architecture

<p>
  The application follows a layered architecture, separating responsibilities
  between controllers, services, repositories, DTOs, mappers and security components.
</p>

<img width="1242" height="386" alt="image" src="https://github.com/user-attachments/assets/c92c9f1f-83ad-45b1-8971-0220be64dd82" />

### I - Main Flow

<img width="1244" height="479" alt="image" src="https://github.com/user-attachments/assets/1d4c14f0-e9ac-43db-9ea0-45036b2780f8" />

The architecture keeps business logic inside the service layer while controllers
are responsible for handling HTTP requests and responses.


## 7 - Main Packages

<pre><code>com.example.eventhub
│
├── config
│
├── controller
│
├── dto
│   ├── request
│   └── response
│
├── entity
│
├── exception
│
├── mapper
│
├── repository
│
├── security
│
├── service
│
└── util
</code></pre>

### I - Package Responsibilities

<table>
  <tr>
    <th>Package</th>
    <th>Responsibility</th>
  </tr>
  <tr>
    <td><code>controller</code></td>
    <td>HTTP endpoints and request handling.</td>
  </tr>
  <tr>
    <td><code>service</code></td>
    <td>Business logic and application rules.</td>
  </tr>
  <tr>
    <td><code>repository</code></td>
    <td>Database access through Spring Data JPA.</td>
  </tr>
  <tr>
    <td><code>entity</code></td>
    <td>JPA entities representing the domain.</td>
  </tr>
  <tr>
    <td><code>dto</code></td>
    <td>Request and response data transfer objects.</td>
  </tr>
  <tr>
    <td><code>mapper</code></td>
    <td>DTO/entity mapping using MapStruct.</td>
  </tr>
  <tr>
    <td><code>security</code></td>
    <td>JWT authentication and Spring Security configuration.</td>
  </tr>
  <tr>
    <td><code>config</code></td>
    <td>Application and AWS-related configuration.</td>
  </tr>
  <tr>
    <td><code>exception</code></td>
    <td>Application exception handling.</td>
  </tr>
</table>



## 8 - Technologies

<table>
  <tr>
    <th>Technology</th>
    <th>Purpose</th>
  </tr>
  <tr>
    <td>Java 25</td>
    <td>Main programming language.</td>
  </tr>
  <tr>
    <td>Spring Boot 4.1.1</td>
    <td>Main application framework.</td>
  </tr>
  <tr>
    <td>Spring Security</td>
    <td>Authentication and authorization.</td>
  </tr>
  <tr>
    <td>JWT</td>
    <td>Stateless authentication.</td>
  </tr>
  <tr>
    <td>Spring Data JPA</td>
    <td>Data persistence.</td>
  </tr>
  <tr>
    <td>Hibernate</td>
    <td>ORM.</td>
  </tr>
  <tr>
    <td>PostgreSQL</td>
    <td>Relational database.</td>
  </tr>
  <tr>
    <td>H2</td>
    <td>Database used in integration tests.</td>
  </tr>
  <tr>
    <td>Testcontainers</td>
    <td>PostgreSQL integration testing.</td>
  </tr>
  <tr>
    <td>JUnit 5</td>
    <td>Unit and integration testing.</td>
  </tr>
  <tr>
    <td>Mockito</td>
    <td>Mocking dependencies in unit tests.</td>
  </tr>
  <tr>
    <td>RestAssured</td>
    <td>REST API integration testing.</td>
  </tr>
  <tr>
    <td>MapStruct</td>
    <td>DTO and entity mapping.</td>
  </tr>
  <tr>
    <td>Docker</td>
    <td>Application containerization.</td>
  </tr>
  <tr>
    <td>Amazon ECS</td>
    <td>Container orchestration.</td>
  </tr>
  <tr>
    <td>AWS Fargate</td>
    <td>Container execution.</td>
  </tr>
  <tr>
    <td>Amazon ECR</td>
    <td>Docker image registry.</td>
  </tr>
  <tr>
    <td>Amazon RDS</td>
    <td>Managed PostgreSQL database.</td>
  </tr>
  <tr>
    <td>Amazon S3</td>
    <td>Event image storage.</td>
  </tr>
  <tr>
    <td>AWS Secrets Manager</td>
    <td>Production secrets management.</td>
  </tr>
  <tr>
    <td>AWS IAM</td>
    <td>Access control and permissions.</td>
  </tr>
  <tr>
    <td>Swagger / OpenAPI</td>
    <td>API documentation.</td>
  </tr>
  <tr>
    <td>Maven</td>
    <td>Dependency and build management.</td>
  </tr>
  <tr>
    <td>Git / GitHub</td>
    <td>Version control.</td>
  </tr>
</table>



## 9 - Testing

<p>
  The project contains unit and integration tests covering business logic,
  REST endpoints, authentication, authorization, database integration and
  Amazon S3 integration.
</p>

### I - Unit Tests

<p>
  Unit tests are implemented using <strong>JUnit 5</strong> and
  <strong>Mockito</strong>.
</p>

The tests cover scenarios such as:

<ul>
  <li>Event business rules</li>
  <li>Event response generation</li>
  <li>Events with and without images</li>
  <li>Pre-signed URL generation</li>
  <li>S3 image upload</li>
  <li>S3 pre-signed URL generation</li>
</ul>

### II - Integration Tests

<p>
  REST API integration tests cover the main application flows.
</p>

<ul>
  <li>Event creation</li>
  <li>Event retrieval</li>
  <li>Event updates</li>
  <li>Event deletion</li>
  <li>Event publishing</li>
  <li>Authentication</li>
  <li>Authorization</li>
  <li>Organizer ownership</li>
  <li>Administrator access</li>
  <li>Multipart requests</li>
  <li>Event image uploads</li>
  <li>HTTP status codes</li>
  <li>Not-found scenarios</li>
</ul>

### III - PostgreSQL Testcontainers

<p>
  PostgreSQL integration testing is also performed using
  <strong>Testcontainers</strong>, allowing the application to be tested
  against a real PostgreSQL database running inside a container.
</p>

<p>
  H2 is also used for specific integration test scenarios.
</p>



## 10 - Configuration

The application uses environment variables for configuration values that should
not be hardcoded into the source code.

### I - Local Environment

Example:

<pre><code>DB_USERNAME=eventhub
DB_PASSWORD=eventhub123
JWT_SECRET=your-local-jwt-secret
JWT_EXPIRATION=3600000</code></pre>

The local Docker Compose environment provides:

<pre><code>Database: eventhub
Username: eventhub
Port: 5432</code></pre>

### II - AWS Environment

Production secrets are managed using AWS Secrets Manager.

The ECS task receives:

<ul>
  <li><code>DB_USERNAME</code></li>
  <li><code>DB_PASSWORD</code></li>
  <li><code>JWT_SECRET</code></li>
  <li><code>JWT_EXPIRATION</code></li>
</ul>

Sensitive production credentials are not stored in the repository.



## 11 - Running the Project

### I - Requirements

<ul>
  <li>Java 25</li>
  <li>Maven</li>
  <li>Docker</li>
  <li>Docker Compose</li>
</ul>

### II - Clone the Repository

<pre><code>git clone &lt;YOUR_GITHUB_REPOSITORY&gt;
cd EventHub</code></pre>

### III - Build the Project

<pre><code>mvn clean package</code></pre>

### IV - Start the Application

<pre><code>docker compose up --build</code></pre>

The application will be available at:

<pre><code>http://localhost:8080</code></pre>



## 12 - Docker

The application is containerized using Docker.

### I - Application Container

The Docker image uses Eclipse Temurin JDK 25.

<pre><code>FROM eclipse-temurin:25-jdk

WORKDIR /app

COPY target/EventHub-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]</code></pre>

### II - Docker Compose

The local environment contains two services:

<pre><code>┌──────────────────────┐
│ EventHub Application │
│      Port 8080       │
└──────────┬───────────┘
           │
┌──────────▼───────────┐
│     PostgreSQL       │
│      Port 5432       │
└──────────────────────┘</code></pre>

Start both services with:

<pre><code>docker compose up --build</code></pre>



## 13 - AWS Infrastructure

The production application is deployed on AWS using managed services.

<img width="871" height="510" alt="image" src="https://github.com/user-attachments/assets/b21218b3-aee5-4e68-8029-f76b7b9234c1" />

### I - Amazon ECS / Fargate

The EventHub application runs as a Docker container using Amazon ECS and AWS Fargate.

Configuration:

<ul>
  <li>0.25 vCPU</li>
  <li>0.5 GiB memory</li>
  <li>1 running task</li>
  <li>Application Load Balancer</li>
  <li>Container health check</li>
</ul>

### II - Amazon ECR

The Docker image is stored in a private Amazon ECR repository.

<pre><code>174302433598.dkr.ecr.us-east-1.amazonaws.com/eventhub</code></pre>

### III - Amazon RDS

The production database uses Amazon RDS with PostgreSQL.

<ul>
  <li>PostgreSQL</li>
  <li><code>db.t4g.micro</code></li>
  <li>20 GiB storage</li>
  <li>Single-AZ</li>
  <li>Private access</li>
  <li>Security Group controlled access</li>
</ul>

### IV - AWS Secrets Manager

Production secrets are stored in AWS Secrets Manager and injected into the ECS task.

### V - AWS IAM

IAM roles are separated according to their responsibilities.

<pre><code>ECS
│
├── Execution Role
│   ├── ECR image pull
│   ├── Secrets Manager
│   └── ECS infrastructure
│
└── Task Role
    └── Amazon S3</code></pre>

The task role is responsible for the permissions required by the application
to interact with the EventHub S3 bucket.



## 14 - Amazon S3

Amazon S3 is used to store event images.

The S3 bucket is private and uses:

<ul>
  <li>Block Public Access</li>
  <li>Bucket owner enforced</li>
  <li>Server-side encryption</li>
  <li>IAM-controlled access</li>
</ul>

### I - Object Structure

Images are stored using the following key pattern:

<pre><code>events/{eventId}/image</code></pre>

Example:

<pre><code>events/22/image</code></pre>

The database stores the object key instead of the complete image URL.

### II - Upload Flow

<img width="968" height="370" alt="image" src="https://github.com/user-attachments/assets/853a4a45-ac83-42f2-a5a0-2be8bc8e5827" />

### III - Pre-signed URLs

When an event is retrieved, the application generates a temporary pre-signed URL.

<img width="918" height="151" alt="image" src="https://github.com/user-attachments/assets/e1203ecd-aa7b-409a-a082-763c9d064505" />

The generated URL is valid for **15 minutes**.

This allows the S3 bucket to remain private while still allowing clients to
display event images.



## 15 - API Documentation

The project uses **Swagger / OpenAPI** for interactive API documentation.

When running locally, Swagger UI is available at:

<pre><code>http://localhost:8080/swagger-ui/index.html</code></pre>

Swagger provides an interactive interface for:

<ul>
  <li>Viewing available endpoints</li>
  <li>Inspecting request and response models</li>
  <li>Testing API endpoints</li>
  <li>Understanding authentication requirements</li>
</ul>



## 16 - Project Goals

The main goals of the EventHub project were:

<ul>
  <li>Develop a complete REST API using Java and Spring Boot.</li>
  <li>Apply layered backend architecture.</li>
  <li>Implement authentication and authorization with Spring Security and JWT.</li>
  <li>Work with relational databases using PostgreSQL and JPA.</li>
  <li>Develop automated unit and integration tests.</li>
  <li>Use Testcontainers for PostgreSQL integration testing.</li>
  <li>Containerize the application using Docker.</li>
  <li>Learn how to deploy a containerized backend to AWS.</li>
  <li>Use managed AWS services such as ECS, RDS, ECR and Secrets Manager.</li>
  <li>Implement private object storage using Amazon S3.</li>
  <li>Use IAM to control access between AWS resources.</li>
</ul>



## 17 - Future Improvements

Possible future improvements include:

<ul>
  <li>CI/CD pipeline</li>
  <li>Application monitoring and observability</li>
  <li>Pagination and additional event filtering</li>
  <li>Rate limiting</li>
  <li>Further performance optimizations</li>
</ul>

<p>
  These features are not part of the current implementation.
</p>



## 18 - Project Versions

### v1.0.0

  EventHub v1.0.0 - Complete backend API

Initial complete backend implementation, including:
<ul>
  <li>REST API</li>
  <li>CRUD operations</li>
  <li>Authentication</li>
  <li>Authorization</li>
  <li>Business rules</li>
  <li>Automated tests</li>
  <li>PostgreSQL integration</li>
</ul>

### v1.1.0

  EventHub v1.1.0 - Dockerized application

Added:
<ul>
  <li>Dockerfile</li>
  <li>Docker Compose</li>
  <li>Containerized application</li>
  <li>Containerized PostgreSQL environment</li>
</ul>

### v1.2.0

  EventHub v1.2.0 - AWS deployment and S3 image storage

Added:
<ul>
  <li>Amazon RDS</li>
  <li>Amazon ECR</li>
  <li>Amazon ECS</li>
  <li>AWS Fargate</li>
  <li>AWS Secrets Manager</li>
  <li>AWS IAM</li>
  <li>Amazon S3</li>
  <li>Event image upload</li>
  <li>Private S3 storage</li>
  <li>Pre-signed URLs</li>
  <li>Production deployment</li>
</ul>
