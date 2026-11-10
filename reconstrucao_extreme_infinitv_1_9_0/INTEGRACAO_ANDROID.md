# Integração Android

## APK de referência

Manifesto declara INTERNET, foreground service e media playback, WAKE_LOCK, POST_NOTIFICATIONS, VIBRATE, RECEIVE_BOOT_COMPLETED e ACCESS_NETWORK_STATE. Declara capacidade Leanback opcional, touchscreen/D-pad opcionais, launcher normal e Leanback, PiP, Activity nativa de vídeo, DreamService, serviço foreground de receiver e FileProvider. O APK adiciona receivers de notificação e inicialização de bibliotecas. `usesCleartextTraffic=true` e `network_security_config` aparecem no wrapper gerado. Consultas package visibility incluem VLC, MX Player e handlers de vídeo.

Bibliotecas Rust `libapp_lib.so` estão empacotadas em quatro ABIs. Pacote tem DEX e bibliotecas AndroidX/Kotlin/Material. Metadados indicam compileSdk 36 e targetSdk 36, minSdk 26.

## Projeto independente

Manifesto próprio declara INTERNET, ACCESS_NETWORK_STATE, WAKE_LOCK, launcher Leanback opcional e touchscreen/D-pad não obrigatórios. Package ID é distinto (`org.readroid.extremerebuild`), reduzindo risco de colisão ou substituição. Media3 fornece playback. Não há serviços persistentes, notificações, PIP, FileProvider ou permissões de armazenamento.

A API minSdk 26 está declarada. Compatibilidade real Android 10+ ainda requer execução nos níveis 29 e posteriores, validação de HTTP cleartext em targetSdk, rede e comportamento em Android TV. Não há ícone/banner Leanback original copiado.
