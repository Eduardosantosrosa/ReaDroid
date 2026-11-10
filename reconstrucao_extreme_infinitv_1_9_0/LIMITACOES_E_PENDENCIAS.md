# Limitações e pendências

## Bloqueios do ambiente de build

Verificações do ambiente Termux: Java/Javac 21 e Gradle estão no PATH. Não localizei `ANDROID_HOME`/`ANDROID_SDK_ROOT`, diretório Android SDK, plataforma, build-tools ou NDK; `adb` não existe. `rustc`, `cargo` e `pnpm` não estão disponíveis. A compilação Tauri Android do original não é viável neste ambiente. O projeto Java independente requer Android SDK API 35 e acesso a Google Maven; `gradle --offline --no-daemon --console=plain assembleDebug` foi tentado e falhou ao resolver `com.android.application:8.6.1`, antes da compilação. APK não foi gerado. A disponibilidade de rede Maven não foi assumida.

## Reconstrução incompleta

- Uma única fonte por vez; sem mirrors, playlists múltiplas, edição, exportação ou backup.
- Parser M3U é intencionalmente básico; não entende playlists master HLS como catálogo, relativas, DRM/Kodi, catch-up, headers, rádios ou duplicação avançada.
- Cliente Xtream cobre auth, categorias e lista de live/VOD/séries; detalhe de temporada/episódio e EPG/XMLTV não existem. Categoria faz requests adicionais.
- Séries mostram nome, mas não abrem temporadas/episódios.
- Player não adiciona headers/custom UA, DRM, PiP, player externo, legendas customizadas, faixas escolhíveis nem serviço foreground.
- Downloads, progresso, recomendações, watchlist, temas, tradução e diagnose estão pendentes.
- UX simples, sem paginação/virtualização; pode consumir muita memória com listas grandes.
- SharedPreferences guarda credenciais sem criptografia do aplicativo. Adicionar Android Keystore antes de distribuir.
- Projeto ainda precisa de lint/compilação, testes de parser/API, dispositivo e validação de acessibilidade. 1.9.0 não implica equivalência funcional comparativa.

## Próximas ações

1. Abrir no Android Studio com SDK API 35, instalar build-tools e baixar dependências Gradle.
2. Compilar e corrigir erros antes de produzir APK debug.
3. Adicionar testes unitários com fixtures M3U e mock server Xtream sem credenciais reais.
4. Implementar persistência segura, EPG, detalhe de série/episódio, downloads e navegação TV.
5. Testar mídia sintética HLS/DASH em celular e TV e medir compatibilidade Android 10+.
6. Revisar política de HTTP cleartext e usar TLS por padrão; validar URLs e redigir credenciais de logs.

## Atualização de auditoria — 2026-11-10

O inventário pré-alteração está em `INVENTARIO_ESTADO_INICIAL.md`; status e matrizes atualizadas estão em `STATUS_REAL_DO_PROJETO.md`, `MATRIZ_DE_FUNCIONALIDADES.md` e `MATRIZ_DE_TESTES.md`. A tentativa Gradle offline para `testDebugUnitTest` não terminou com resultado e foi interrompida durante a inicialização do daemon. Não houve APK. A falta do SDK e do plugin 8.6.1 no cache impede build confiável neste Termux; ver `INSTRUCOES_DE_COMPILACAO.md` para ambiente Windows/Android Studio.

A implementação observada tem apenas uma fonte, sem Xtream de detalhes/EPG, sem série jogável, sem Keystore e sem fluxos de biblioteca/downloads/backup. Os quatro casos JUnit existentes são escritos, não executados. A matriz nova classifica esses limites sem converter superfícies de interface em funcionalidade concluída.

## Situação após a implementação Android — 2026-11-10

Os parágrafos acima registram o estado inicial e foram superados pelas alterações descritas nas matrizes e no `STATUS_REAL_DO_PROJETO.md`. O ambiente local possui SDK API 35 e gerou Debug; 7 JUnit passaram e testes instrumentados compilam. Não confundir compilação instrumentada com execução: falta emulador local; Actions precisa confirmar execução para a versão atual.

Pendências reais: validar navegação/toque/D-pad em celulares e TV Box físicos; validar playback, formatos, headers, PiP e seleção de faixas contra streams e codecs reais; teste com provedor Xtream real; suportar XMLTV e busca realmente federada; downloads de streams HLS/DASH segmentadas (fila atual cobre arquivos HTTP diretos); validar retomada/estado do serviço após reinício do Android. O app permite cleartext por compatibilidade com listas/hosts legados, portanto deve ser usado com fontes confiáveis. Não há segredo de assinatura configurado, então não alegar APK Release assinado.

O workflow final `42891896745` executou com sucesso JUnit, instrumentação no emulador API 35 e geração/upload Debug. O workflow Python separado do repositório continua falhando em testes de banner já existentes e preservados; ver `MATRIZ_DE_TESTES.md`.
