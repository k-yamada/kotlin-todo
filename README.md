# API の動作確認

terminal から curl でリクエストを発行して動作確認します。

## POST /todos

```
$ curl -i -X POST -H "Content-Type: application/json" -d '{"title":"test1", "description":"hoge"}' localhost:8080/todos
```

## GET /todos

```
$ curl -i localhost:8080/todos
```

## GET /todos/{id}

```
$ curl -i localhost:8080/todos/1
```
