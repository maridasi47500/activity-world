mkdir dev-resources/test-certs -p
cd dev-resources/test-certs
openssl genrsa -aes256 -out privkey.pem 2048
openssl rsa -pubout -in privkey.pem -out pubkey.pem

