# Player e streaming

## Original

Frontend inclui `player-runtime`, integração Android nativa e seletor de player, trilhas de áudio/legendas, escala de vídeo, atraso de legenda, PiP, watchdog de travamento, medição e diagnóstico. Dependências listadas: Video.js, Artplayer, hls.js, mpegts.js, Shaka Player e mp4box. Rust tem proxies de VOD/áudio e suporte HTTP range; Android gerado inclui `VideoActivity`, layout próprio de controles de TV, serviço foreground de receiver e Media3 1.10.1 (ExoPlayer, HLS, DASH, RTSP, UI e session). Isso mostra caminhos implementados, não garante todos os codecs/servidores.

URLs podem incluir credenciais Xtream; original possui construção de URL, headers de stream e proteção para dados sensíveis em diagnóstico. Reprodução MPEG-TS depende do caminho de player e contêiner; manifesto Kodi com DRM é reconhecido no parser M3U, mas não se presume que seja universalmente reproduzível.

## Reconstrução

`VideoActivity` usa Media3 1.5.1 com módulos ExoPlayer/HLS/DASH e controles `PlayerView`. `MediaItem.fromUri` recebe a URL entregue pelo catálogo. Erros são apresentados com código Media3. Network cleartext é permitido no manifesto para compatibilidade com provedores que ainda usam HTTP; recomenda-se HTTPS. Nenhum relay é usado, portanto stream e credenciais embutidas na URL vão diretamente do dispositivo ao servidor escolhido.

HLS/DASH dependem dos respectivos módulos; compatibilidade MPEG-TS, RTSP, codecs, DRM, áudio alternativo, legendas externas e URLs autenticadas com headers não foi validada. Sem teste em dispositivo/servidor controlado, reprodução efetiva permanece não verificada.
