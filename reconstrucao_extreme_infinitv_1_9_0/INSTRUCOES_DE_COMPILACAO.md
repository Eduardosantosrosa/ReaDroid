# Instruções de compilação

## Requisitos

- Android Studio com suporte a AGP 8.6.1.
- JDK 17 selecionado em **Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JDK**.
- Gradle 8.7 (compatível com AGP 8.6.1), incluído pelo wrapper `./gradlew`.
- Android SDK Platform 35 e Android SDK Build-Tools 35.x instalados pelo SDK Manager.
- Acesso Maven/Google Maven para baixar AGP e dependências Media3 1.5.1/JUnit 4.13.2.

## Compilar e testar

Abra `reconstrucao_extreme_infinitv_1_9_0` como projeto no Android Studio. Aguarde a sincronização e execute `:app:testDebugUnitTest`, depois `:app:assembleDebug`. Alternativamente, em terminal com JDK 17 e SDK configurado:

```sh
./gradlew --no-daemon --console=plain :app:testDebugUnitTest
./gradlew --no-daemon --console=plain :app:assembleDebug
```

O APK debug é gravado em `app/build/outputs/apk/debug/app-debug.apk`. Para verificá-lo, use Android Studio Analyze APK ou `apkanalyzer`/`aapt` e instale em emulador/dispositivo separado. Debug usa assinatura local e não substitui nem atualiza o app original.

## Release assinado

Crie uma chave própria fora do repositório e mantenha cópia segura do arquivo e das senhas. Não reutilize a chave do aplicativo de referência e nunca faça commit do keystore ou das senhas. No shell, configure `ANDROID_KEYSTORE_FILE` para o caminho do keystore, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS` e `ANDROID_KEY_PASSWORD`; então execute `./gradlew --no-daemon :app:assembleRelease`. Com essas variáveis, Gradle assina o APK em `app/build/outputs/apk/release/app-release.apk`. Sem a variável de arquivo, o build Release não tem assinatura de distribuição configurada.

No GitHub Actions, configure os secrets `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS` e `ANDROID_KEY_PASSWORD`. O workflow gera um artifact Release assinado quando todos estiverem presentes; sem eles, apenas Debug é gerado. O keystore decodificado fica no diretório temporário do runner.

## Termux

Neste ambiente o SDK está em `$HOME/android-sdk`, com API 35/Build Tools 35.0.0; configure `ANDROID_HOME` e `ANDROID_SDK_ROOT` para esse caminho. Use JDK 17 para Gradle (há override do AAPT2 experimental fornecido pelo ambiente Termux). Exemplo:

```sh
ANDROID_HOME="$HOME/android-sdk" ANDROID_SDK_ROOT="$HOME/android-sdk" ./gradlew --offline --no-daemon --console=plain :app:testDebugUnitTest :app:assembleDebug
```

Essa sequência foi executada com sucesso aqui. Não há binário Android Emulator instalado, portanto não execute `connectedDebugAndroidTest` localmente; o workflow usa um emulador hospedado.

Esta reconstrução Java não deve ser confundida com o projeto Tauri original, que adicionalmente exige Node/pnpm, Rust/Cargo e integração Tauri Android. Não há afirmação de compilação do projeto original.
