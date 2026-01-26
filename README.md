# notification-service

Микросервис для отправки email-уведомлений пользователям.

Поддерживает два режима работы:
- **асинхронный** — получение событий из Kafka (создание / удаление пользователя)
- **синхронный** — REST API для ручной отправки email

Проект разработан как отдельный независимый микросервис и предназначен для работы совместно с `user-service`.

---

## 🧰 Prerequisites

Для локального запуска требуется:

- **Java 21**
- **Docker** и **Docker Compose**
- **Gradle Wrapper** (уже включён в репозиторий)

---

## 🐳 Local development (Docker)

### Общая идея

`notification-service` может работать в двух режимах инфраструктуры:

- использовать **общую Kafka**, поднятую из `user-service` (рекомендуется для локальной разработки);
- поднимать **собственную Kafka** (standalone-режим).

Оба варианта поддерживаются через `docker-compose` и профили.

---

### Option A — shared Kafka from `user-service` (recommended)

1. Запусти Kafka в `user-service`:
```bash
docker compose up -d
```

2. Запусти инфраструктуру `notification-service`:
```bash
docker compose up -d
```

3. Запусти приложение:
```bash
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun
```

Используемые переменные окружения:
```env
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
USER_EVENTS_TOPIC=user.notifications
```

---

### Option B — standalone Kafka (infra profile)

```bash
docker compose --profile infra up -d
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun
```

---

## 🚀 Quick Start

### Прогон тестов

```bash
./gradlew clean test
```

---

## 🔌 Используемые порты

| Сервис                | Порт |
|----------------------|------|
| notification-service | 8085 |
| MailHog (SMTP)       | 1025 |
| MailHog (Web UI)     | 8025 |
| Kafka                | 9092 |

---

## 📬 REST API

`POST /api/v1/notifications/email`

---

## 📡 Kafka режим

Топик:
```
user.notifications
```

Формат сообщения:
```json
{
  "operation": "CREATED",
  "email": "user@test.local"
}
```

---

## ⚙️ Профили

| Профиль | Назначение |
|-------|-----------|
| dev | Swagger UI |
| test | Kafka off |

---

## 📎 Связанные проекты

- https://github.com/hodkonem/user-service