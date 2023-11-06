## Бот «душнила» для Telegram.

#### Просто очередной Telegram бот. Основная функция – это невероятно душно указывать людям на их ошибки в правописании. Для проверки используется Yandex Speller. Тоже не самый простой персонаж, со своей спецификой.

##### Так же имеет несколько дополнительных функций:   
* Ведёт статистику грамотеев в канале.  
* Умеет игнорировать конкретные слова, специфичные для канала.   
* Умеет искать слова в сообщении и реагировать на них словом или фразой.   
* Если кто-то до сих пор не умеет печать несмотря на клавиатуру, и выдаёт эпичный монолог латиницей, хотя хотелось кириллицей. То этот товарищ тоже выручит.     
* Через «личку» общается с админом. Там необходимо разрешать новые каналы, после добавления на них бота, ну или не разрешать. Можно смотреть статистику работы.   
* Все настройки игнорирования, замены, статистика, уникальны для каждого канала.  
* И в канале и в «личке» всегда послушно поможет по команде /help.   

Структура базы данных – это стыд, я знаю. Была заложена в самом начале, еще на этапе проб. Но пока переделывать не планирую. Этой хватает. Нагрузка минимальна.

#### Настройка проще чем сходить под дождём за хлебом:  
Прописываем в application.properties:  
* Имя бота  
* Токен бота  
* Telegram ID админа бота (цифровой)  
* Реквизиты для доступа к БД (PostgreSQL)

```
bot.name=<BOT_NAME>
bot.token=<BOT_TOKEN>
bot.admin=<BOT_ADMIN_TG_ID>

spring.datasource.driverClassName = org.postgresql.Driver
spring.datasource.url = jdbc:postgresql://<DB_HOST>:<DB_PORT>/<DB_NAME>
spring.datasource.username = <DB_USER>
spring.datasource.password = <DB_PASSWORD>
```

#### P.S. Это мой первый Pet проект на Java. Прошу сильно не пинать. Но конструктивная критика всегда приветствуется :)
