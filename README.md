## onyx-http

Onyx plugin for http.

#### Installation

In your project file:

```clojure
[onyx-http "0.8.4.0"]
```

In your peer boot-up namespace:

```clojure
(:require [onyx.plugin.http-output])
```

#### Functions

##### sample-entry

Catalog entry:

```clojure
{:onyx/name :entry-name
 :onyx/plugin :onyx.plugin.http-output/output
 :onyx/type :output
 :onyx/medium :http
 :http-output/success-fn :my.namespace/success?
 :onyx/batch-size batch-size
 :onyx/doc "POST segments to http endpoint"}
```

Lifecycle entry:

```clojure
[{:lifecycle/task :your-task-name
  :lifecycle/calls :onyx.plugin.http/lifecycle-calls}]
```

#### Attributes

|key                           | type      | description
|------------------------------|-----------|------------
|`:http-output/success-fn`     | `keyword` | Accepts response as argument, should return boolean to indicate if the response was successful. Request will be retried in case it wasn't a success.

#### Contributing

Pull requests into the master branch are welcomed.
