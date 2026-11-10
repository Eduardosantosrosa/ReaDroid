# Instruções de compilação

## Requisitos para Windows com Android Studio

- Android Studio com suporte a AGP 8.6.1.
- JDK 17 selecionado em **Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JDK**.
- Gradle 8.7 (compatível com AGP 8.6.1). Este repositório não inclui `gradlew`; selecione Gradle 8.7 no ambiente de build ou adicione wrapper revisado em mudança própria.
- Android SDK Platform 35 e Android SDK Build-Tools 35.x instalados pelo SDK Manager.
- Acesso Maven/Google Maven para baixar AGP e dependências Media3 1.5.1/JUnit 4.13.2.

## Compilar e testar

Abra `reconstrucao_extreme_infinitv_1_9_0` como projeto no Android Studio. Aguarde a sincronização e execute `:app:testDebugUnitTest`, depois `:app:assembleDebug`. Alternativamente, em terminal com `gradle` 8.7 e SDK configurado:

```sh
gradle --no-daemon --console=plain :app:testDebugUnitTest
gradle --no-daemon --console=plain :app:assembleDebug
```

O APK debug esperado após sucesso real é `app/build/outputs/apk/debug/app-debug.apk` dentro deste projeto. Para verificá-lo, use Android Studio Analyze APK ou `apkanalyzer`/`aapt` e instale em emulador/dispositivo separado. Debug usa assinatura local e não substitui nem atualiza o app original.

## Termux desta auditoria

Não há SDK (`ANDROID_HOME`/`ANDROID_SDK_ROOT` ausentes, sem `android.jar`/`sdkmanager`), nem JDK 17 configurado (JDK 21 presente), e o cache não contém AGP 8.6.1. O cache possui AGP 8.7.3, que não corresponde à configuração e exigiria uma versão Gradle diferente. O comando offline de teste não concluiu. Não instalar toolchain grande nem baixar dependências foi necessário; portanto não há APK gerado neste ambiente.

Esta reconstrução Java não deve ser confundida com o projeto Tauri original, que adicionalmente exige Node/pnpm, Rust/Cargo e integração Tauri Android. Não há afirmação de compilação do projeto original.
