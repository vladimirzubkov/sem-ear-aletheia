# Semestrální práce B6B36EAR – Aletheia

**Autor:** Vladimir Zubkov
**Předmět:** B6B36EAR – Enterprise architektury
**Projekt:** Aletheia – systém pro rozvrhování univerzitních kurzů
**Dokumentace:** [Aletheia_SRS.pdf](Documentation/Aletheia_SRS.pdf), aktualizované diagramy

---

## 1. Popis aplikace a její struktury

Aletheia je backendová webová aplikace postavená na frameworku **Spring Boot**, která slouží k plánování a správě univerzitních rozvrhů. Systém umožňuje administrátorům vytvářet kurzy a sekce (přednášky, cvičení), zatímco studenti se mohou zapisovat na konkrétní termíny s automatickou kontrolou kapacit a časových kolizí.

### Architektura

Aplikace dodržuje vrstvenou architekturu s důrazem na oddělení zodpovědností (Separation of Concerns):

* **REST Layer (`rest`):** controller třídy, které přijímají HTTP požadavky, validují vstupy a delegují práci na servisní vrstvu. Využívá se DTO (Data Transfer Objects) pro oddělení API kontraktu od vnitřního datového modelu.
* **Service Layer (`service`):** obsahuje veškerou business logiku a transakční zpracování (`@Transactional`). Klíčové služby jsou `EnrollmentService` (řešení kolizí, kontrola kapacit) a `CourseService`.
* **Data Access Layer (`dao`):** rozhraní Repository (Spring Data JPA) pro komunikaci s databází.
* **Domain Model (`model`):** JPA entity využívající dědičnost (`InheritanceType.JOINED`) pro polymorfní zpracování sekcí (`Section` -> `LectureSection`, `SeminarSection`).
* **Security (`security`):** implementace Spring Security s vlastní autentifikací (`CustomUserDetailsService`) a autorizací na základě rolí (ADMIN, STUDENT, TEACHER).

---

## 2. Návod na instalaci a spuštění

Aplikace je připravena ke spuštění v režimech: lokálně s in-memory databází pro testy, se školním Postgres (slon) nebo v kontejneru Docker. Databáze je ve výchozím stavu nastavena na opětovné vytvoření při každém spuštění. Je-li potřeba pouze update, v `main/resources/application.properties` je vhodné změnit `spring.jpa.hibernate.ddl-auto=create` na `update`. Ke spuštění ve Windows kořenovém adresáři projektu jsou spouštěcí `batch` soubory, počínající od `0` a které je vhodné takto postupně spouštět:

<u>Základní funkčnost aplikace:</u>

* **0_mvn_clean_package.bat** (`mvn clean package`) - aplikace se zkompiluje, průběžně se zkotrolují unit a integrační testy,
* **1_start.bat** (`java -jar target/Aletheia-1.0-SNAPSHOT.jar`) - aplikace se spustí,
* **2_run_scenarios.bat** - spustí se scénáře pro End-to-End testy interakce s databázi, CRUD. Je to stejný jako spuštění soubory **scenarios.http** z Intellij IDEA.

<u>Bonus:</u>

* **3_docker.bat** (`docker-compose up --build`) - aplikace se spustí v Dockeru (démon Dockeru musí být spuštěn). Poté lze znovu testovat scénaře z předchozího bodu.

### Prerekvizity

* Java 17+ (JDK)
* Maven
* Docker (volitelné)

Při startu aplikace třída `GeneratorConfig` automaticky detekuje prázdnou databázi a naplní ji testovacími daty (uživatelé, kurzy, zápisy) .

### Přístup k aplikaci

* Bude potřeba logování, viz. dole: http://localhost:8080/
* Kontrola že běží: http://localhost:8080/actuator/health
* Swagger UI (REST API dokumentace): http://localhost:8080/swagger-ui/index.html, je rovněž dostupné v podobě souboru
  * JSON: http://localhost:8080/v3/api-docs
  * YAML: http://localhost:8080/v3/api-docs.yaml

### Přihlašovací údaje (generované)

* **Admin:** `tony.stark` / `password`
* **Student:** `marty.mcfly` / `password`
* **Učitel:** `obiwan.kenobi` / `password`

---

## 3. Splnění požadavků CP2

Přehled implementovaných bodů z Checkpoint 2 (rovněž vizte okomentovaný PDF soubor `courses_b6b36ear_cp2....pdf`):

* Pokročilé techniky JPA (min. 3):
  * **Ordering (`@OrderBy`):** použito v entitě `Course` pro řazení kolekce seminářů podle kapacity (`@OrderBy("capacity DESC")`).
  * **Named Queries (`@NamedQuery`):** definováno v entitě `Course` pro optimalizované vyhledávání podle kódu předmětu.
  * **Kaskádní operace (`CascadeType.ALL`):** použito pro vazby `Course` -> `LectureSection` a `Section` -> `TimeSlot`. Smazání kurzu korektně odstraní i jeho sekce (pokud nejsou zapsaní studenti).
  * **Criteria API:** implementováno v `CourseService` pro dynamické vyhledávání kurzů podle názvu a počtu kreditů.

* Architektura a logika:
  * **Transakční zpracování:** anotace `@Transactional` na úrovni servisní vrstvy zajišťuje atomicitu operací (zejména při zápisu a tvorbě kurzů)
  * **Netriviální CRUD:** operace nepracují jen s jednou tabulkou, ale zasahují do provázaných entit (User, Enrollment, Section, Course) s kontrolou integrity.
  * **Security:** implementována autentizace (DB) a autorizace (Role: ADMIN, STUDENT). Metody API jsou zabezpečeny podle rolí.

* Ověření a testování:
  * **Integrační scénáře:** Soubor `scenarios.http` (a odpovídající `.bat` skript) pokrývá kompletní životní cyklus dat (Create Course -> Read -> Enroll -> Update Capacity -> Delete).
  * **DataJpaTest:** Unit testy pro `EnrollmentService` ověřují složitou business logiku (překryvy časových slotů, kapacity).

* Bonus:
  * **Docker:** Aplikace je plně kontejnerizována (Dockerfile + docker-compose).
  * **Swagger UI:** Integrována dokumentace API pomocí SpringDoc.

---

## 4. Získané zkušenosti a řešení problémů

Během vývoje této semestrální práce jsem narazil na několik architektonických výzev, které mi pomohly lépe pochopit principy Enterprise aplikací.

### a) Modelování dědičnosti v JPA vs. Vazba 1:1

Narazil jsem na dilema při návrhu vztahu `Course` a `LectureSection`. Ačkoliv má kurz aktuálně pouze jednu hlavní přednášku (vazba 1:1), rozhodl jsem se neslučovat tyto tabulky do jedné.

* **Důvod:** využil jsem polymorfismus. `LectureSection` dědí od abstraktní `Section`. To mi umožnilo v `EnrollmentService` pracovat s jakoukoliv sekcí (přednáškou i cvičením) jednotně. Nakonec kurz v budoucnu může mít více sekcí, tj. více přednášek.
* **Výhoda:** pokud se v budoucnu rozhodne, že kurz bude mít více paralelních přednášek, změna bude triviální (změna na 1:N) bez nutnosti refaktoringu logiky zápisů.

### b) Testování business logiky

Rozdělil jsem strategii testování na dvě části:

1. **Unit/Integration testy (`EnrollmentServiceTest`):** pro složitou logiku, jako je detekce časových kolizí a "double booking", jsem použil `DataJpaTest`. To mi umožnilo ověřit všechny hraniční stavy.
2. **API Scénáře (.http / curl):** pro standardní CRUD operace v `CourseService` jsem nepsal unit testy (což by vedlo jen k mockování repository), ale vytvořil jsem integrační scénáře, které ověřují celý průchod systémem od Controlleru až po DB.

### c) Spring Security a Role

Implementace vlastního `UserDetailsService` mi ukázala flexibilitu Spring Security. Místo hardcodovaných uživatelů načítám uživatele z DB a dynamicky jim přiřazuji role na základě toho, jestli je entita instance třídy `Admin`, `Student` nebo `Teacher`. To zjednodušilo správu oprávnění.

### d) Dockerizace

Přidání `Dockerfile` a `docker-compose` v závěrečné fázi se ukázalo jako velmi užitečné pro ověření, že aplikace není závislá na lokálním prostředí mého počítače ("it works on my machine" problém).

### e) Ověření datového modelu (Inheritance JOINED)

Při kontrole fyzického modelu databáze jsem si v praxi ověřil, jak Hibernate mapuje dědičnost strategií `JOINED` do relačních tabulek.

* **Pozorování:** Společná data uživatelů jsou uložena v hlavní tabulce `app_user`. Specifické tabulky rolí (jako `student`, `teacher`) obsahují pouze primární klíč, který slouží zároveň jako cizí klíč odkazující zpět do `app_user`.
* **Výhoda:** Tímto způsobem nedochází k duplikaci dat (jméno a heslo jsou uloženy jen jednou) ani k plýtvání místem (v hlavní tabulce nejsou prázdné sloupce NULL pro atributy, které daná role nemá).

**Ukázka z reálného exportu databáze:**

*Tabulka `app_user` (společná data `AbstractUser`):*

| id | username | first_name | last_name | email |
|---:|:---|:---|:---|:---|
| 3 | obiwan.kenobi | ObiWan | Kenobi | hello.there@jedi.org |
| 4 | marty.mcfly | Marty | McFly | marty.mcfly@hillvalley.edu |

*Tabulka `student` (podtřída `AbstractUser`):*

| id | (FK -> app_user) |
|---:|:---|
| 4 | *(Marty je student)* |

*Tabulka `teacher` (podtřída `AbstractUser`):*

| id | (FK -> app_user) |
|---:|:---|
| 3 | *(ObiWan je učitel)* |


---
Konec dokumentace. Děkuji za pozornost. 
V Praze dne 4. ledna 2026.