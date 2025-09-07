try no to put to much obvios name in thing for john deere.
git only use development but i kmnow it should be in branches fro each feature and more.
the model is created for avoiding damage when the message data changes. comon.
not tdd but test as soon and as much as i can.
to run mock whitelist autehtication and local queue initialization star de proyect with -Dspring.profiles.active=dev
you acantest authorized client and mock with curl --location 'http://localhost:8080/api/machines/30/authorized'
many ways to better it up: chain of responsabilit patter in service or something like that. or use aws sqs templates and async clients. separar variabels pòr entorno, TRANDSACCIONALIDD SI NO PUBLICA NO GUARDA Y ASI.
# JohnMessaging

Backend demo para procesamiento de mensajes de maquinaria usando SQS (LocalStack).

## 🚀 Requisitos
- Docker y Docker Compose instalados
- (opcional) Maven y JDK 17 si quieres compilar localmente

## ▶️ Arranque rápido

compose build
compose up (queda en dv para que prueben como seria pero sin dev queda apuntando a las variables del configuration)
solo es descargar el probgrama y hacer los pasos del compose. 