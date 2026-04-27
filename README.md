# Система управления банковскими картами

Система управления банковскими картами с JWT-аутентификацией, ролевой моделью (ADMIN/USER), шифрованием данных и переводами между своими картами.

---

## 📋 Оглавление

- [Технологии](#технологии)
- [Функциональность](#функциональность)
- [Запуск проекта](#запуск-проекта)
- [API Эндпоинты](#api-эндпоинты)
- [Структура проекта](#структура-проекта)
- [Документация](#документация)

---

## 🛠 Технологии
| Технология | Версия | Назначение |
|-----------|--------|------------|
| Java | 21 | Язык программирования |
| Spring Boot | 3.2.0 | Основной фреймворк |
| Spring Security | 6.2.0 | Аутентификация и авторизация |
| Spring Data JPA | 3.2.0 | Работа с базой данных |
| PostgreSQL | 15 | Реляционная база данных |
| Liquibase | 4.24.0 | Управление миграциями |
| JWT (JJWT) | 0.12.3 | JSON Web Token |
| Kafka | 3.6.0 | Очереди сообщений |
| MapStruct | 1.5.5 | Маппинг Entity ↔ DTO |
| Lombok | 1.18.30 | Генерация кода |
| Swagger/OpenAPI | 2.2.0 | Документация API |
| Docker | 27.x | Контейнеризация |
| Maven | 3.11.0 | Сборка проекта |

---

## ✨ Функциональность

### 👑 Администратор (ADMIN)
| Операция | Эндпоинт |
|----------|----------|
| Создание карты | `POST /api/cards` |
| Блокировка карты | `POST /api/cards/{id}/block` |
| Активация карты | `POST /api/cards/{id}/activate` |
| Удаление карты | `DELETE /api/cards/{id}` |
| Просмотр всех карт | `GET /api/cards` |
| Управление пользователями | `GET/POST/DELETE /api/users` |

### 👤 Пользователь (USER)
| Операция | Эндпоинт |
|----------|----------|
| Просмотр своих карт (пагинация) | `GET /api/cards/user/{userId}?page=0&size=10` |
| Просмотр баланса карты | `GET /api/cards/{id}` |
| Запрос на блокировку карты | `POST /api/cards/{id}/request-block?userId={userId}` |
| Перевод между своими картами | `POST /api/transactions/transfer` |
| История транзакций | `GET /api/transactions/card/{cardId}` |

### 🔐 Безопасность
- JWT-аутентификация (токен передаётся в заголовке `Authorization: Bearer <token>`)
- Ролевая модель: `ADMIN` и `USER`
- Пароли пользователей хранятся в зашифрованном виде (BCrypt)
- Номера карт хранятся в зашифрованном виде в БД
- В API номера карт возвращаются в маскированном виде: `**** **** **** 1234`

---

## 🚀 Запуск проекта

### Требования
- Java 21
- Docker Desktop
- Maven


### Шаг 1: Клонирование репозитория
```bash
git clone <your-repo-url>
cd bank_rest-main
```

### Шаг 2: Запуск базы данных и Kafka
```bash
docker-compose up -d
```

### Шаг 3: Сборка проекта
```bash
mvn clean install
```
### Шаг 4: Запуск приложения
```bash
mvn spring-boot:run
```
### Шаг 5: Проверка работы
Открой в браузере:
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- API Docs: http://localhost:8080/v3/api-docs

### Шаг 6: Остановка
```bash
# Остановить приложение — нажми Ctrl+C в консоли

# Остановить Docker контейнеры
docker-compose down

# Остановить с удалением всех данных БД
docker-compose down -v
```

## 📝 API Эндпоинты

### Аутентификация

| Метод | URL | Описание |
|-------|-----|----------|
| POST | `/api/auth/login` | Вход в систему (возвращает JWT токен) |
| POST | `/api/users/register` | Регистрация нового пользователя |

### Пользователи (только ADMIN)

| Метод | URL | Описание |
|-------|-----|----------|
| GET | `/api/users` | Список всех пользователей |
| GET | `/api/users/{id}` | Получение пользователя по ID |
| DELETE | `/api/users/{id}` | Удаление пользователя |

### Карты

| Метод | URL | Роль | Описание |
|-------|-----|----|----------|
| POST | `/api/cards` | ADMIN | Создание карты |
| GET | `/api/cards` | ADMIN | Все карты |
| GET | `/api/cards/{id}` | USER | Просмотр баланса карты |
| GET | `/api/cards/user/{userId}` | USER | Карты пользователя (пагинация) |
| DELETE | `/api/cards/{id}` | ADMIN | Удаление карты |
| POST | `/api/cards/{id}/deposit` | USER, ADMIN | Пополнение карты |
| POST | `/api/cards/{id}/withdraw` | USER, ADMIN | Списание с карты |
| POST | `/api/cards/{id}/block` | ADMIN | Блокировка карты |
| POST | `/api/cards/{id}/activate` | ADMIN | Активация карты |
| POST | `/api/cards/{id}/request-block` | USER | Запрос на блокировку |

### Транзакции

| Метод | URL | Описание |
|-------|-----|----------|
| POST | `/api/transactions/transfer` | Перевод между своими картами |
| GET | `/api/transactions/card/{cardId}` | История транзакций по карте |

## 📁 Структура проекта

```
src/main/java/com/example/bankcards/
├── config/                 # Конфигурации (Security, Swagger, CORS, Kafka)
├── controller/             # REST контроллеры
├── dto/                    # Data Transfer Objects
├── entity/                 # JPA сущности
├── entity/enums/           # Перечисления (CardStatus, Role)
├── exception/              # Кастомные исключения + GlobalExceptionHandler
├── mapping/                # MapStruct мапперы
├── repository/             # Spring Data JPA репозитории
├── security/               # JWT, фильтры, UserDetailsService
├── service/                # Бизнес-логика
└── util/                   # Утилиты (шифрование, маскирование, даты)

src/main/resources/
├── application.yml         # Конфигурация приложения
├── db/migration/           # Liquibase миграции
│   ├── db.changelog-master.yaml
│   ├── v1_create_users_table.yaml
│   ├── v2_create_cards_table.yaml
│   ├── v3_create_transactions_table.yaml
│   └── v4_add_operation_type_column.yaml
└── docs/
    └── openapi.yaml        # OpenAPI документация

docker-compose.yml          # Docker Compose для dev-среды
pom.xml                     # Maven конфигурация
```

## 📊 Примеры запросов (c URL)

### 1. Регистрация пользователя
```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123","role":"ADMIN"}'
```
### 2. Вход в систему (получение JWT токена)
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```
### 3. Создание карты (ADMIN)
```bash
curl -X POST http://localhost:8080/api/cards \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-token>" \
  -d '{"userId":1,"cardNumber":"1234567812345678","expirationDate":"2028-12-31","balance":1000,"cardStatus":"ACTIVE"}'
```
### 4. Перевод между своими картами
```bash
curl -X POST http://localhost:8080/api/transactions/transfer \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-token>" \
  -d '{"purpose":"transfer","amount":200,"fromCardId":1,"toCardId":2}'
```
### 5. Просмотр карт пользователя (пагинация)
```bash
curl -X GET "http://localhost:8080/api/cards/user/1?page=0&size=10" \
  -H "Authorization: Bearer <your-token>"
```
### 6. Просмотр баланса карты
```bash
curl -X GET http://localhost:8080/api/cards/1 \
  -H "Authorization: Bearer <your-token>"
```
### 7. История транзакций по карте
```bash
curl -X GET http://localhost:8080/api/transactions/card/1 \
  -H "Authorization: Bearer <your-token>"
```
### 8. Запрос на блокировку карты (USER)
```bash
curl -X POST "http://localhost:8080/api/cards/1/request-block?userId=1" \
  -H "Authorization: Bearer <your-token>"
```
### 9. Блокировка карты (ADMIN)
```bash
curl -X POST http://localhost:8080/api/cards/1/block \
  -H "Authorization: Bearer <your-token>"
```
### 10. Активация карты (ADMIN)
```bash
curl -X POST http://localhost:8080/api/cards/1/activate \
  -H "Authorization: Bearer <your-token>"
```

## 📖 Документация

- **Swagger UI:** http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs
- **OpenAPI YAML:** `docs/openapi.yaml`