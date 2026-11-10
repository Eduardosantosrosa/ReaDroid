# Comparação APK e código-fonte

## Correspondências observadas

| Evidência | Fonte | APK | Leitura |
|---|---|---|---|
| Identidade/versionamento | `tauri.conf.json`: identifier, version 1.9.0, android.versionCode 109009000 | `aapt`: com.infinitel8p.xtream, 1.9.0, 109009000 | Correspondência exata |
| Min/target SDK | manifesto Android gerado e Gradle | aapt: min 26 / target 36 | Correspondência |
| Runtime Tauri/Rust | Cargo lib `app_lib`, plugin Rust Tauri | `libapp_lib.so` em 4 ABIs | Compatível |
| Player Android | projeto gerado declara `VideoActivity`, Media3 | manifesto traz `com.infinitel8p.xtream.VideoActivity`; libs AndroidX no APK | Correspondência estrutural |
| TV | manifesto e layouts/rotas TV | Leanback launcher, banner e DreamService | Correspondência |

## Divergências/notas

- Fonte `src-tauri/gen/android` registra permisos de instalação e feature capabilities; APK tem também permissões/receivers gerados por dependências (`RECEIVE_BOOT_COMPLETED`, `ACCESS_NETWORK_STATE`, notification receivers e AndroidX startup).
- Manifesto fonte define `network_security_config`, FileProvider, consultas de players externos e serviços; manifesto binário mostra componentes e políticas expandidas por merger. É comportamento esperado do merge Gradle, sem sinal observado de incompatibilidade.
- APK é artefato assinado com certificado de distribuição `InfiniteL8p`; build local de debug teria outra chave e não seria atualizável sobre a instalação original.
- Assinatura verificada por `apksigner`: V2, SHA-256 de certificado `1d3ab36fe1380db82e30ec1d86b43f0690eb6139383cd4ee4a5cdc8608ed46e2`. SHA-256 do APK: `357eeee79f8d2d0054efec451e6d5fca7a3e84ee57c768a30ea7d8b401f7023a`; SHA-256 do ZIP de fonte: `735bee2f8b78230cd668f94ddb1c8f294af139a1a482c6225a70d1fd68971c81`.

Nenhuma análise de comportamento foi feita por execução, instrumentation ou instalação. Não se extraiu/decompilou o DEX para comparar tela por tela; portanto a comparação confirma identidade/configuração e componentes, não paridade de lógica em código compilado.
