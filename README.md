# NTT Skyway Test
Basic test of [NTT Skyway](https://webrtc.ecl.ntt.com/en/).

There are two folders:
* `android` contains the Android Studio project
* `webapp` contains a NodeJS Express webapp


## android
Add a `secret.properties` file at the root of `android/` folder with the following contents:
```
SKYWAY_API_KEY="<insert-your-api-key-here>"
SKYWAY_DOMAIN="<insert-your-domain-here>"
```


## webapp
Add a `.env` file at the root of the `webapp/` folder with the following contents:
```
SKYWAY_API_KEY="API-KEY"
```

Make sure you have NodeJS installed. Then run:
```
npm install
npm start
```

Open up a web-browser and go to https://localhost:8080.
