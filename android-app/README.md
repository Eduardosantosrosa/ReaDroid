# Catálogo de demonstração ReaDroid

App Android nativo em Java, independente e sem bibliotecas de terceiros. O
conteúdo é fictício e local. A navegação implementada é home → categoria →
detalhe. Não há autenticação, rede, transmissão nem reprodução de mídia.

## Executar testes locais

```sh
sh run-tests.sh
```

## Gerar APK debug

Com Gradle 8.9+ e Android SDK 35 instalados:

```sh
ANDROID_HOME="$HOME/android-sdk" ANDROID_SDK_ROOT="$HOME/android-sdk" \
  gradle --offline --console=plain assembleDebug
```

O APK sai em `app/build/outputs/apk/debug/app-debug.apk`. Pelo menu do
ReaDroid, a opção 17 executa os testes, compila offline e acompanha/valida o
arquivo gerado.

Consulte [IMPLEMENTATION_STATUS.md](IMPLEMENTATION_STATUS.md) para evidências,
inferências e limitações.
