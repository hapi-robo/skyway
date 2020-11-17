if (process.env.NODE_ENV !== 'production') {
  require('dotenv').config();
}

const express = require('express');
const path = require("path");

// constants
const port = process.env.PORT || 8080;

// instantiate webapp
const app = express();

// setup template engine
app.set('view engine', 'ejs');

// serve static files
app.use(express.static(path.join(__dirname, "assets")));

// set up routes
app.get("/", (req, res) => 
  res.render("index", {
    API_KEY: process.env.SKYWAY_API_KEY
  })
);

app.listen(port, () => {
  console.log(`App is listening on port ${port}`)
});
