# Plano de implementação

1. **P0 — build reproduzível e base:** instalar toolchain documentada em máquina de build; compilar e corrigir erros reais; executar JUnit; adicionar testes de persistência e Xtream mock; documentar versões. Não declarar APK antes de validar artefato.
2. **P0 — dados seguros:** substituir fonte única por modelo versionado de fontes; migração dos dados atuais; guardar segredos com Android Keystore; não registrar URLs com credenciais; cobrir reinício e falhas.
3. **P1 — IPTV:** CRUD e seleção de múltiplas fontes; importar M3U por URL e documento escolhido; preservar metadados e serializar M3U; validar status, timeout, JSON e cancelamento; filtros por categoria sem requests repetidos.
4. **P2 — catálogo:** detalhes de filme e série; temporadas/episódios; XMLTV, fuso horário e now/next; favoritos e histórico por fonte; busca e persistência paginada.
5. **P3 — player:** criar sessão/ciclo de vida confiável Media3; URLs autenticadas com headers protegidos; HLS/DASH/TS segundo suporte do aparelho; seleção de áudio/legenda; erros acionáveis; testes com mídia sintética.
6. **P4 — Android TV:** shell adequado a 720p/1080p, foco visível, D-pad integral, back consistente, launcher Leanback e acessibilidade; validar em TV/emulador.
7. **P5 — adicionais:** downloads, edição/importação/exportação de playlist, backup/restauração, watchlist, preferências e temas.

Cada incremento deve ter comportamento real, persistência apropriada e testes registrados em `MATRIZ_DE_TESTES.md`. O plano não declara os itens concluídos.
