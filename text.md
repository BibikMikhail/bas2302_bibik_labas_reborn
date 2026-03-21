# Что делать руками (лаб. 6): цепочка НЕ в Java-коде

## Важно понять

| Что | Где это |
|-----|---------|
| **Создать 3 сертификата** | Не в `src/main/java`. Запускаешь **скрипт** + **OpenSSL** на своём ПК. |
| **Приложение** | Только **читает** готовый файл `*.p12` из переменных `SSL_KEY_*`. Это уже настроено в `application-https.properties`. |

В репозитории лежит **инструкция и скрипт**. Сами `.crt` / `.p12` появятся **у тебя на диске** после запуска скрипта (папка `certs/` в `.gitignore` — в Git не попадает).

---

## Шаг 1. Установи OpenSSL (если нет)

Часто уже есть с **Git for Windows**:  
`C:\Program Files\Git\usr\bin\openssl.exe`

---

## Шаг 2. Открой PowerShell в **корне проекта** (где `pom.xml`)

```powershell
cd "d:\YandexDisk\Yandex.Disk\rpbo"
```

---

## Шаг 3. Запусти генерацию цепочки (3 звена + билет 1БАС25001 в OU)

```powershell
.\scripts\generate-tls-chain.ps1 -P12Password "ПридумайСложныйПароль123"
```

Билет по умолчанию уже **1БАС25001**. Если нужен другой:

```powershell
.\scripts\generate-tls-chain.ps1 -StudentTicket "1БАС25001" -P12Password "..."
```

После этого на диске появится папка:

`certs\rpbo-chain\`

Там будут:

- `01-root.crt` — корень
- `02-intermediate.crt` — промежуточный
- `03-server.crt` — серверный
- `mtuci-rpbo-api-chain.p12` — то, что нужно Spring

**Это и есть твоя цепочка из трёх звеньев** (три `.crt` + один `.p12` с ключом и цепочкой для Tomcat).

---

## Шаг 4. Переменные окружения (как выведет скрипт в конце)

Пример (подставь **свой** пароль от шага 3):

```powershell
setx SSL_KEY_STORE "file:./certs/rpbo-chain/mtuci-rpbo-api-chain.p12"
setx SSL_KEY_STORE_PASSWORD "ПридумайСложныйПароль123"
setx SSL_KEY_ALIAS "mtuci-rpbo-leaf-key"
```

Закрой терминал, открой **новый** (после `setx` так надо).

---

## Шаг 5. Запуск приложения с TLS

```powershell
cd "d:\YandexDisk\Yandex.Disk\rpbo"
$env:SPRING_PROFILES_ACTIVE="https"
mvn spring-boot:run
```

В логе должно быть что-то вроде порта **8443** и **https**.

---

## Шаг 6. Показать преподавателю цепочку

1. Импортируй **`certs\rpbo-chain\01-root.crt`** в «Доверенные корневые» (как в `docs/TLS_CHAIN.md`), чтобы браузер не ругался.
2. Открой `https://localhost:8443`
3. Замок → **Сертификат** → вкладка **Путь сертификации** — **три уровня**
4. В **Составе** сертификата — поле **OU** = `StudentTicket-1БАС25001`

---

## Если препод спросил про keytool

Можно сказать: *«Для лабы сначала делал один self-signed через keytool; по требованию цепочки из трёх звеньев сделал отдельную PKI через OpenSSL и скрипт в репозитории — так корректно строят root → intermediate → server.»*

---

## Что «добавлять в код» не нужно

Ничего в Java дописывать для **генерации** цепочки не требуется. Уже есть:

- `application-https.properties` — включить TLS и указать keystore через переменные
- скрипт `scripts/generate-tls-chain.ps1` — **создать** файлы цепочки

Твоя работа — **один раз выполнить шаги 3–6**.
