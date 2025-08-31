curl -X POST http://localhost:8080/action_create_news \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -H "X-CSRF-Protection: your-token-here" \
     -d "title=Hello&photo=somefile"

