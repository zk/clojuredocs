The ClojureDocs API is used (mainly) to support dynamic content on the site.

## Requests

### Authentication

Most POSTs, PUTs, and DELETEs require authentication, where most GETs
don't. See specific endpoint documentation for requirements.

### Parameters

Parameters for GET and DELETE requests are sent as either part of the
request path (`GET /api/examples/:id`), or as query parameters (`GET
/api/examples?sort=desc`).

### Request Bodies

POST and PATCH requests bodies are [EDN](https://github.com/edn-format/edn) encoded.


## Responses

ClojureDocs responds with EDN encoded bodies.


### Errors

Here are some common errors you'll encounter using the ClojureDocs API:

Failure to send an EDN encoded body:

```
;; HTTP/1.1 415 Unsupported Media Type

{:message "Request must be Content-Type application/edn"}
```


Sending invalid EDN

```
HTTP/1.1 400 Bad Request

{:message "Error parsing request body as EDN"}
```

Trying to POST / DELETE while unauthenticated

```
HTTP/1.1 401 Unauthorized

{:message "Unauthorized"}
```

Payload validation errors

```
HTTP/1.1 422 Unprocessable Entity

{:errors [{:field "body" :message "Body can't be empty"}
          {:field "var"  :message "That var doesn't exist"}]}
```

Occasionally, you might run into a server error

```
HTTP/1.1 500 Internal Server Error

{:message "There was a problem processing your request"}
```
