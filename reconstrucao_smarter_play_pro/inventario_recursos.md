# Inventário de recursos

- APK: 84.091.340 bytes; SHA-256 `81fa604fadc64869ea8239e0366178c50d12b80db88f40b1a4b56369ae495ff4`.
- Android min 17, target/compile 30; atividades multi-orientação/Leanback.
- `classes.dex`, `classes2.dex`, `assets/audience_network.dex`.
- ARM64 inclui FFmpeg/IJK, codecs ExoPlayer (AV1/VPx/Opus/FLAC), OpenVPN e crypto; empacotamento não comprova uso em cada fluxo.
- Recursos `res/*` extensos/ofuscados, fontes Open Sans, executáveis OpenVPN em quatro ABIs e assets de demonstração/termos. Nada será copiado.
- Activities para login, dashboard, Live/VOD/séries, EPG, player, ajustes, backup e mídia local; providers FileProvider/próprio; componentes Firebase/OpenVPN. Inventário integral do manifesto pendente.
- Permissões: Internet, rede/Wi-Fi, boot, armazenamento legado, áudio, overlay, foreground service, wake lock, Firebase e billing; declaração não prova uso.
- Ferramentas: JADX 1.5.6, Apktool, AAPT/AAPT2, apksigner, jarsigner, Gradle 9.9, Java 21, unzip e strings. `android.jar` em `$PREFIX/share/java/android.jar`; SDK formal não detectado.
