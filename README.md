# Proje Adı: Hedef Takip Simülasyonu
## Proje Tanımı
Bu proje, iki kule ve bir hareketli hedefin bulunduğu bir senaryoyu simüle eder. İlk kulede bir radar, ikinci kulede ise bir kamera bulunur. Radar, hedefin konum bilgilerini toplar ve bu bilgileri kullanarak kameranın hedefi takip etmesini sağlar. Bu süreç, Apache Kafka ve Docker kullanılarak gerçekleştirilir.

## Kullanılan Teknolojiler
 Java: Uygulama geliştirme dili.
Apache Kafka: Mesaj kuyruğu hizmeti sunar, uygulamalar arasında veri aktarımını sağlar.
Docker: Uygulamaların ve hizmetlerin kapsayıcılar içinde çalışmasını sağlar.
JavaFX: Grafik kullanıcı arayüzü (GUI) oluşturmak için kullanılır.
## Proje Bileşenleri
Proje dört ana Java uygulaması ve bazı destekleyici hizmetlerden oluşur:

#### 1. world_simulator
Amacı: Hedefin hareketini simüle eder ve Kafka'ya konum bilgilerini gönderir.
#### 2. radar_control
Amacı: Hedefin konum bilgilerini alır, açısal pozisyon ve mesafe bilgilerini hesaplar ve Kafka'ya gönderir.
#### 3. camera_control
Amacı: Radar tarafından gönderilen açısal pozisyon bilgilerini alır ve kameranın açısını günceller.
#### 4. world_gui
Amacı: Hedefin ve kulelerin durumlarını grafiksel olarak gösteren JavaFX tabanlı arayüz uygulaması.
#### 5. Zookeeper
Amacı: Kafka'nın çalışması için gerekli olan koordinasyon hizmetlerini sağlar.
#### 6. AKHQ
Amacı: Kafka yöneticisi olarak kullanılır, Kafka kümelerini izlemek ve yönetmek için grafiksel bir arayüz sunar.
## Sistem Mimarisi
Sistem, aşağıdaki bileşenlerden oluşur:

Zookeeper: Kafka'nın çalışması için gerekli olan koordinasyon hizmetlerini sağlar.
Kafka Broker: Uygulamalar arasında veri aktarımını sağlayan mesaj kuyruğu hizmeti sunar. Topikler: TargetPointPosition, TowerPosition, CameraLosStatus, TargetBearingPosition.
AKHQ: Kafka yöneticisi olarak kullanılır, Kafka kümelerini izlemek ve yönetmek için grafiksel bir arayüz sunar.
## Veri Akışı
world_simulator uygulaması, hedefin konum bilgilerini üretir ve TargetPointPosition topiğine gönderir.
radar_control uygulaması, TargetPointPosition topiğinden gelen verileri alır, açı ve mesafe bilgilerini hesaplar ve TargetBearingPosition topiğine gönderir.
camera_control uygulaması, TargetBearingPosition topiğinden gelen açı bilgilerini alır ve kameranın açısını günceller, ardından CameraLosStatus topiğine gönderir.
world_gui uygulaması, tüm bu bilgileri alır ve grafiksel arayüzde gösterir.
## Kurulum ve Başlatma
### 1. Docker Ortamının Hazırlanması
Docker Compose dosyasını kullanarak gerekli hizmetleri başlatmak için aşağıdaki adımları izleyin:

Bu repo'yu klonlayın:

```
git clone repository_url
```
```
cd repository_directory
```
Docker Compose dosyasını çalıştırın:

```
docker-compose up -d
```
Bu komut, Zookeeper, Kafka ve AKHQ hizmetlerini kapsayıcılar içinde başlatacaktır.
Resources klasörü içerisinde yml dosyası verilmiştir.
Docker üzerinde harici uygulamaların ayağa kaldırılmasının sonrasında topiklerin manuel olarak oluşturulması gerekmektedir.
Aşağıda ilgili komutlar verilmiştir:
```

docker exec -it kafka-container-id /bin/bash


# TargetPointPosition topiği
kafka-topics --create --topic TargetPointPosition --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1

# TargetBearingPosition topiği
kafka-topics --create --topic TargetBearingPosition --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1

# CameraLosStatus topiği
kafka-topics --create --topic CameraLosStatus --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1

# TowerPosition topiği
kafka-topics --create --topic TowerPosition --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
```


### 2. Uygulamaları Çalıştırma
Her bir uygulamayı kendi IDE'nizde veya komut satırında çalıştırabilirsiniz. Aşağıdaki komutları kullanarak uygulamaları çalıştırın:

world_simulator
```
java -jar path/to/your/world_simulator.jar
```

radar_control
```
java -jar path/to/your/radar_control.jar
```

camera_control
```
java -jar path/to/your/camera_control.jar
```
### 3. AKHQ ile Kafka Yönetimi
AKHQ, Kafka kümelerini izlemek ve yönetmek için grafiksel bir arayüz sunar. AKHQ'ya erişmek için tarayıcınızda http://localhost:8080 adresine gidin. Varsayılan kullanıcı adı ve şifre admin/admin'dir.

AKHQ ile Kafka topiklerini izleyebilir, mesajları görebilir ve Kafka kümelerinin durumunu kontrol edebilirsiniz.

## Önemli Noktalar ve Önlemler
Hata İşleme ve Loglama: Tüm uygulamalarda olası hatalar kontrol edilmekte ve loglanmaktadır. Bu, hata ayıklama ve izleme süreçlerini kolaylaştırır.
Thread Yönetimi: Uygulamalarda kullanılan thread'ler düzgün bir şekilde yönetilmektedir. Uygulamalar düzgün kapanması için shutdown hook kullanır.
Kaynak Yönetimi: Kafka producer ve consumer nesneleri düzgün bir şekilde kapatılır. Bu, kaynakların doğru bir şekilde yönetilmesini sağlar.
Kapsayıcı Sağlığı: Docker Compose dosyasında sağlık kontrolleri eklenmiştir. Bu, hizmetlerin sağlıklı çalışıp çalışmadığını kontrol eder ve gerektiğinde yeniden başlatır.
Sonuç
Bu proje, iki kule ve bir hareketli hedefin bulunduğu bir senaryoyu simüle eder. Hedefin konum bilgileri world_simulator uygulaması tarafından üretilir, radar_control uygulaması tarafından işlenir ve camera_control uygulaması tarafından kameranın açısı güncellenir. Tüm bu süreçler Kafka aracılığıyla gerçekleşir ve AKHQ ile izlenebilir.
