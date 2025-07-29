# Example of Lambda with managed Java 21 runtime using Amazon Aurora DSQL with PgJDBC and Hikari datasource pool 

## Architecture



## Installation and deployment

Unfortunately it's currently not possible to create DSQL cluster with AWS SAM, so please use AWS CLI for it.  
See the description https://docs.aws.amazon.com/aurora-dsql/latest/userguide/getting-started.html  
I created a single-region DSQL cluster with: aws dsql create-cluster --region us-east-1  


```bash

Clone git repository locally
git clone https://github.com/Vadym79/AWSLambdaJavaWithAmazonDSQL.git


Compile and package the Java application with Maven from the root (where pom.xml is located) of the project
mvn clean package

Deploy your application with AWS SAM
sam deploy -g --region us-east-1

Please provide your Aurora DSQL cluster id (not the endpoint!) as an input for the variable AuroraDSQLClusterId like jkliueisyb4ghfunxgzgjklll
```
Now you API Gateway has been deployed and you have some REST endpoint like get for /orders/{id} and post for /orders and so on

Use this Http Body as Json to create a sample order with 2 items :

{"userId":12345,"totalValue":350, "status":"RECEIVED",
 "orderItems":[{"productId":230, "value":100,"quantity":3},{"productId":233, "value":250,"quantity":3}]
} 

or with [hey tool](https://github.com/rakyll/hey)

hey -q 1 -z 1m -c 1 -m POST -d '{"userId":12345,"totalValue":350, "status":"RECEIVED", "orderItems":[{"productId":230, "value":100,"quantity":3},{"productId":233, "value":250,"quantity":3}]}' -H "X-API-Key: a6ZbcDefQW12BN56WEDS7" -H "Content-Type: application/json;charset=utf-8"  ${API_GATEWAY_URL}/prod/orders/

for update order status use specific order use orders/updatestatus/{id} and status like SHIPPED as body


  

## In oder to use it you're required to

1) Connect to the already created Aurora DSQL cluster using CloudShell, see the desciption here  
 https://docs.aws.amazon.com/aurora-dsql/latest/userguide/getting-started.html#connect-dsql-cluster and here  
 https://docs.aws.amazon.com/aurora-dsql/latest/userguide/getting-started.html#accessing-sql-clients-psql
 
2) Execute these sql statements to create table and sequences   

CREATE TABLE orders (id int PRIMARY KEY,  user_id  int NOT NULL, total_value int NOT NULL, status varchar (255) NOT NULL ); 

CREATE TABLE order_items (id int PRIMARY KEY,  product_id int NOT NULL, order_id int NOT NULL, value int NOT NULL, quantity int NOT NULL);

CREATE INDEX ASYNC order_items_order_id_idx ON order_items (order_id);

3) Populate some data

INSERT INTO orders VALUES (1, 12345, 250, 'RECEIVED'); 

INSERT INTO order_items VALUES (1, 79900, 1, 150, 1); 
INSERT INTO order_items VALUES (2, 79901, 1, 100, 2); 


INSERT INTO orders VALUES (2, 24678, 200, 'RECEIVED'); 

INSERT INTO order_items VALUES (3, 79900, 2, 50, 1); 
INSERT INTO order_items VALUES (4, 79902, 2, 150, 3); 
