db = db.getSiblingDB("admin");

db.createUser({
  user: "millo",
  pwd: "1234",
  roles: [
    { role: "readWrite", db: "ilhayoung_db" },
    { role: "read", db: "admin" }
  ]
});
