# BillBook

## Libraries

---

- Picasso : For Image Loading
- Retrofit : for communication with APIs/backend
- OkHttp : For Logging purposes
- Dexter _(to use in Future)_ : Permission Manager

## Version Upgrade Details

---

- Changes to be done in `app/build.gradle`

> Suppose current _version code_ is : **1.12.7** (a.b.c)

- bug fix /small modification :
  - increase **c** (1.12.8)
- new feature / new UI /moderate changes :
  - increase **b** & **reset c** (1.13.0)
- new db schema/ migration to Kotlin or any heavy changes :
  - Increase **a** and **reset b and c** (2.0.0)

## Release History Details

---

### Version : 1.26.2

---

> Planned

- Fix Whatsapp Share on first time
- Fix search invoice, invoices order getting messed up after editing
- Permission Screen like OLA for Storage

> Ready to go

- Allow any characters in TextField (Hindi Characters...etc)
- Navigation drawer _Company Logo_ update
