# Reconstrução Android independente

Aplicativo Android em Java/Media3, separado da implementação de referência. Usa análise estática do material fornecido e não reutiliza código, imagens ou identidade visual do original. O código desta reconstrução está licenciado sob MIT; confirme as licenças dos serviços e dependências antes de distribuir.

## Funcionalidades implementadas no código

- Fontes M3U remotas/locais e Xtream com cadastro, edição, seleção e exclusão; credenciais, URLs de fontes, biblioteca e URLs de mídia armazenadas com AES-GCM via Android Keystore.
- Categorias de catálogo, pesquisa sobre o catálogo carregado, favoritos, Assistir depois e histórico.
- Reprodução Media3 de URLs reconhecidas, HLS e DASH, cabeçalhos M3U, seleção de faixas de áudio/legenda, PiP quando suportado e opção de autoplay.
- Xtream: autenticação/listagem, detalhes VOD, episódios por temporada, EPG curto e tratamento de respostas limitadas/erros sanitizados.
- Editor de entradas M3U em memória, exportação M3U; backup/restauração portátil cifrado por frase secreta.
- Downloads diretos HTTP resumíveis de arquivos de mídia, fila privada do app e reprodução offline.
- Interface com controles focáveis, launcher Android TV/Leanback, layout adaptável e SAF para arquivos.

Isso documenta código presente, não compatibilidade comprovada com cada provedor, codec ou controle remoto. A busca global limita-se ao catálogo que estiver carregado; arquivos HLS/DASH segmentados não são empacotados para download offline. Consulte `MATRIZ_DE_FUNCIONALIDADES.md`, `MATRIZ_DE_TESTES.md` e `LIMITACOES_E_PENDENCIAS.md` para evidência e lacunas.

## Build e testes

AGP 8.6.1, Gradle 8.7, JDK 17, Android SDK 35. Execute `./gradlew :app:testDebugUnitTest :app:assembleDebug`; os testes instrumentados rodam em emulador via workflow. Consulte `INSTRUCOES_DE_COMPILACAO.md`. APK local previamente validado: `app/build/outputs/apk/debug/app-debug.apk` (provisório; artefato remoto final depende do workflow mais recente).

Release assinado requer os quatro secrets descritos nas instruções. Não adicione playlists, credenciais ou keystore ao Git. O código se conecta apenas aos hosts/fontes configurados pelo usuário.
