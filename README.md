# Okul Yönetim Sistemi

## Proje Konusu
Öğrencilerin ders kayıtlarını, notlarını ve başarı durumlarını takip etmek amacıyla geliştirilmiş kapsamlı bir sistem. Bu arka uç (backend) uygulaması, öğrenci bilgilerini güvenli bir şekilde saklamayı, ders kayıt işlemlerini yönetmeyi ve "Akıllı Rapor" özellikleriyle detaylı analizler sunmayı verimli bir şekilde ele alır.

## Kurulum Kılavuzu
### Gereksinimler
- JDK 17 veya üzeri
- IntelliJ IDEA (Community veya Ultimate) veya başka bir Java IDE'si
- Docker ve Docker Compose (Konteyner kurulumu için)
- Maven
- PostgreSQL (Docker tercih edilmeyecekse yerel kurulum için)

### Docker ve Veritabanı Kurulumu
1. [Docker Desktop](https://www.docker.com/products/docker-desktop/)'ı işletim sisteminize uygun şekilde indirin.
2. İndirilen kurulum dosyasını çalıştırın ve ekrandaki talimatları izleyin.
3. Kurulum tamamlandıktan sonra Docker Desktop'ı başlatın ve çalıştığından emin olun.
4. Projenin kök dizininde bir terminal açın.
5. Veritabanını ve uygulamayı izole bir şekilde başlatmak için `docker compose up --build` komutunu çalıştırın.
6. Konteynerler başarıyla ayağa kalktığında PostgreSQL veritabanı hazır olacaktır. (Varsayılan port 5434 olarak ayarlanmıştır).

### IntelliJ IDEA Proje Kurulumu
1. IntelliJ IDEA'yı açın.
2. "File > Open" seçeneğini seçerek proje dizinini seçin ve "OK" butonuna tıklayın.
3. Projede Maven kullanıldığından dolayı IDEA bağımlılıkları (Spring Boot, Hibernate, PostgreSQL Driver vb.) otomatik olarak yükleyecektir.
4. Eğer yerel veritabanı kullanılacaksa veritabanı bağlantı ayarlarını kontrol edin:
   - Proje içindeki `src/main/resources/application.yml` veya `application.properties` dosyasını açın.
   - URL, kullanıcı adı ve parolayı kendi PostgreSQL kurulumunuza göre düzenleyin.

### Projeyi Çalıştırma
1. IDE üzerinden uygulamanın ana başlatıcı dosyasını (örneğin `Application.java`) bulun.
2. Dosya üzerinde sağ tıklayın ve "Run" seçeneğini seçin. (Veya terminalden `mvn spring-boot:run` komutunu çalıştırın).
3. Uygulama başlatıldığında `http://localhost:8080` adresinden sisteme erişebilirsiniz.
4. Swagger API dokümantasyonu için `http://localhost:8080/swagger-ui/index.html` adresini ziyaret edebilirsiniz.

### Olası Sorunlar ve Çözümleri
- **Veritabanı Bağlantı Hatası:** Docker'ın çalıştığından ve PostgreSQL konteynerinin aktif olduğundan emin olun.
- **Bağımlılık Hataları:** Maven'ın projeyi tam olarak indirdiğinden emin olun (`mvn clean install` komutunu deneyebilirsiniz).
- **Port Çakışması Hatası:** 8080 portunun veya veritabanı portunun başka bir uygulama tarafından kullanılmadığından emin olun.
- **Compile Hataları:** JDK versiyonunuzun (17) proje ile uyumlu olduğundan emin olun.

## Projenin Yapısı
Projede farklı paketlerde organize edilmiş bir Spring Boot dizin yapısı bulunmaktadır:

```text
├── src/main/java/.../
│   ├── controller/      # API isteklerini karşılayan iş mantığı sınıfları
│   ├── model/           # Varlık sınıfları (Entity ve DTO'lar)
│   ├── repository/      # Veritabanı erişim sınıfları (DAO)
│   ├── service/         # Temel iş mantığının yürütüldüğü sınıflar
│   └── App.java         # Ana çalıştırma sınıfı
├── src/main/resources/  # Konfigürasyon ve statik dosyalar
│   ├── application.yml  # Ayar dosyası
│   └── static/          # Statik web dosyaları (arayüz)
└── docker-compose.yml   # Docker yapılandırma dosyası
```

## Teknik Detaylar
### Kullanılan Veritabanı Türü
- PostgreSQL veritabanı
- Spring Data JPA (Hibernate) kullanımı mevcut
- PostgreSQL Driver (JDBC) kullanıldı

### Kullanılan Geliştirme Ortamı
- IntelliJ IDEA
- Spring Boot
- Maven proje yapısı
- Docker & Docker Compose

### PostgreSQL Avantajları:
- Güçlü ve açık kaynaklı ilişkisel veritabanı yönetim sistemidir (RDBMS)
- Karmaşık sorgular ve büyük veri setleri ile yüksek performans gösterir
- Veri bütünlüğü (ACID prensipleri) tam olarak desteklenir

### PostgreSQL Dezavantajları:
- Kurulumu ve yapılandırması hafif dosya tabanlı veritabanlarına göre (örn. SQLite) daha fazla kaynak gerektirir
- Çok basit projeler için gereğinden karmaşık olabilir

## Mimari
### Katmanlı Mimari:
- **Entity Katmanı:** Varlık sınıfları (Öğrenci, Ders, Kayıt)
- **Repository (DAO) Katmanı:** Veritabanı işlemlerini yönetir
- **Service (Business) Katmanı:** İş mantığını yönetir
- **Controller Katmanı:** REST API üzerinden dışarıya hizmet sunar

### Kapsülleme ve Temiz Kod:
- Değişkenler private, erişim için getter/setter metodları kullanılmış
- Kod yapısı sade ve modüler tutulmuş

### Nesne Yönelimli İlkeler:
- **Builder Pattern:** Öğrenci gibi karmaşık nesnelerin yaratılması düzenli bir şekilde sağlandı
- **Observer Pattern:** Not girişlerinde sistem bileşenleri tetiklenerek log/bildirim işlemleri otomatize edildi
- Katmanlar arası bağımlılıklar arayüzler ve bağımlılık enjeksiyonu (Dependency Injection) ile yönetiliyor

### Değişkenler - Koşullar - Döngüler
- Her varlık sınıfında özel (private) değişkenler tanımlandı
- Koşullu yapılar Service sınıflarında iş kuralları (geçme notu hesaplama vs.) için kullanıldı
- Veritabanından çekilen liste verilerini işlemek için Java Stream API ve döngüler kullanıldı

## Projenin Özellikleri
- Kullanıcı (Öğrenci) kaydı ve profili oluşturma
- Ders arama, listeleme ve yönetimi
- Derslere kayıt fonksiyonelliği
- Not girişi ve değerlendirme işlemleri
- Akıllı raporlama ve harf notu hesaplaması

## Geliştirmek İçin Neler Yapılabilir?
**Güvenlik ve Yetkilendirme (Spring Security):**
- Projeye Admin, Öğretmen ve Öğrenci rolleri eklenerek JWT (JSON Web Token) tabanlı güvenli oturum yönetimi sağlanabilir.

**Kapsamlı Bir Frontend Entegrasyonu:**
- Statik arayüz yerine React, Vue veya Angular gibi modern bir framework ile tam donanımlı bir web arayüzü tasarlanabilir.

**Mikroservis Mimarisine Geçiş:**
- Proje büyüdüğünde not sistemi, öğrenci kayıt sistemi gibi modüller ayrı mikroservislere bölünebilir.

**Cache (Önbellek) Kullanımı:**
- Redis gibi teknolojiler kullanılarak çok sık okunan veriler (ders listesi vb.) önbelleğe alınıp performans artırılabilir.

**Login Güvenliği ve Hashleme:**
- Kullanıcı parolaları veritabanında şifrelenerek tutulmalı ve güvenli oturum yönetimi sağlanmalı.

## Olumlu Yönler
- Katmanlı mimari ile yapılandırıldı (SOLID'e uygun)
- Design Pattern (Observer, Builder) kullanımlarıyla daha sağlam bir mimari kuruldu
- Docker ile kurulum ve çalıştırma çok basit hale getirildi
- RESTful standartlarına uyumlu ve Swagger ile test edilebilir API'ye sahip

## Olumsuz Yönler
- Kimlik doğrulama (Authentication) ve yetkilendirme katmanı eksik
- Verilen varsayılan web arayüzü (frontend) modern bir SPA değil
- Kullanıcıların parolalarını kurtarabileceği mail servisleri vs. henüz entegre edilmedi

## Bu Projeyle Neler Yapılabilir?
- Okullar, üniversiteler veya kurslar için çekirdek (core) bir yönetim sistemi olarak kullanılabilir
- Ücretli/ücretsiz ders kayıt modülleri entegre edilerek geliştirilebilir
- Çevrimiçi yetkilendirme (Role-based access) entegre edilebilir
- Backend temeli olarak kullanılarak modern frontend ile bütünleştirilebilir
- İleri seviye bir portföy projesi olarak özgeçmişe eklenebilir

## Hazırlayanlar
- Didem ZEREN, Emin SANCAKLI, Hilal YILDIZ
