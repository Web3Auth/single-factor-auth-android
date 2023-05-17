# Single-Factor-Auth-Android

[![](https://jitpack.io/v/com.github.web3auth/single-factor-auth-android.svg)](https://jitpack.io/#com.github.web3auth/single-factor-auth-android)

> Web3Auth is where passwordless auth meets non-custodial key infrastructure for Web3 apps and wallets. By aggregating OAuth (Google, Twitter, Discord) logins, different wallets and innovative Multi Party Computation (MPC) - Web3Auth provides a seamless login experience to every user on your application.

Web3Auth Single Factor Auth is the SDK that gives you the ability to start with just one key (aka, Single Factor) with Web3Auth, giving you the flexibility of implementing your own UI and UX.


## 📖 Documentation

Checkout the official [Web3Auth Documentation](https://web3auth.io/docs/sdk/web/core/) to get started.


## Features
- Multi network support
- All API's return `CompletableFutures`


## Getting Started

Typically your application should depend on release versions of fetch-node-details, but you may also use snapshot dependencies for early access to features and fixes, refer to the Snapshot Dependencies section.
This project uses [jitpack](https://jitpack.io/docs/) for release management

Add the relevant dependency to your project:

```groovy
repositories {
        maven { url "https://jitpack.io" }
   }
   dependencies {
         implementation 'com.github.web3auth:single-factor-auth-android:0.0.1'
   }
```

### Permissions

Open your app's `AndroidManifest.xml` file and add the following permission:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

## Requirements
- Android API version 24 or newer is required.

## 🩹 Examples

Checkout the examples for your preferred blockchain and platform in our [examples repository](https://github.com/Web3Auth/single-fact-auth-android/tree/master/app)


## 💬 Troubleshooting and Discussions

- Have a look at our [GitHub Discussions](https://github.com/Web3Auth/Web3Auth/discussions?discussions_q=sort%3Atop) to see if anyone has any questions or issues you might be having.
- Checkout our [Troubleshooting Documentation Page](https://web3auth.io/docs/troubleshooting) to know the common issues and solutions
- Join our [Discord](https://discord.gg/web3auth) to join our community and get private integration support or help with your integration.
