# Funcionalidades observadas e estado da reconstrução

Legenda: **Original evidenciado** significa código implementado encontrado, sem afirmar teste funcional. **Reconstrução** descreve este projeto novo.

| # | Área | Evidência no original | Reconstrução atual |
|---:|---|---|---|
| 1 | Xtream Codes | login, credenciais, API e informação de conta | Implementado: autenticação e streams/categorias |
| 2 | M3U/M3U8 | `m3u-parser`, importação e exportação | Parcial: leitura HTTP e EXTINF básica |
| 3 | Múltiplas playlists | credenciais/playlist switcher | Pendente |
| 4 | Live TV | `livetv`, catálogo live | Parcial: listagem/categoria |
| 5 | Categorias | filtros e editores | Parcial: filtro de categoria do provedor |
| 6 | Busca canais | SearchView e filtros | Parcial: filtro em memória |
| 7 | EPG | parser/worker, mapping, now-next, página `/epg` | Pendente |
| 8 | Catch-up | `catchup`, resolução, timeshift | Pendente |
| 9 | Replay | player e cálculo de timeshift | Pendente |
| 10 | VOD filmes | catálogo, detalhe e API Xtream | Parcial: lista e URL de filme |
| 11 | Séries | catálogo, temporadas, episódios e detalhes | Parcial no original; pendente na reconstrução |
| 12 | Favoritos | views/strips e preferências | Parcial: persistência local básica |
| 13 | Watchlist | página e strips dedicados | Pendente |
| 14 | Histórico/progresso | continue-watching/progresso | Pendente |
| 15 | Recomendações | because-watched, similar-local, trending | Pendente |
| 16 | Busca global | página e search view | Parcial: pesquisa local atual |
| 17 | Player interno | player runtime e várias bibliotecas web | Parcial: Media3 fullscreen |
| 18 | HLS | hls.js, Media3 e resolução de streams | Parcial: Media3 HLS |
| 19 | MPEG-TS | mpegts.js, player e proxy | Não validado; dependente do demux/media stack |
| 20 | MPEG-DASH | Shaka e dependência Media3 DASH | Parcial: Media3 DASH |
| 21 | Legendas | tracks, seleção e atraso | Pendente |
| 22 | Faixas de áudio | seleção/proxy de áudio | Pendente |
| 23 | Player externo | launcher, intents e escolha de app | Pendente |
| 24 | Downloads offline | página/lib downloads e storage Android | Pendente |
| 25 | Editor M3U | editor, custom playlist e serializador | Pendente |
| 26 | Backup/restauração | backup snapshot e restore | Pendente |
| 27 | Temas | settings e accent/theme | Pendente |
| 28 | Idiomas | i18n com 16 arquivos de locale | Pendente |
| 29 | Configuração de rede | timeout, fetch, headers, conectividade | Parcial: timeout básico no cliente |
| 30 | D-pad | navegação espacial e layouts TV | Foco padrão Android, sem navegação customizada |
| 31 | Android TV | layout/rotas TV e flags Leanback | Parcial: launcher Leanback/foco nativo |
| 32 | PIP | player e auto-PIP, manifesto | Pendente |
| 33 | Receiver/cast | discovery, protocolo e serviço foreground | Pendente |
| 34 | Notificações mídia | service e integração de sessão | Pendente |
| 35 | Dream/screen saver | XtreamDreamService e ambient views | Pendente |
| 36 | Diagnose/health | probes, health dialog e logs | Pendente |

## Critério de contagem

A contagem de 36 agrupa superfícies funcionais encontradas; alguns itens do original têm implementação parcial ou dependem do provedor, de rede, de outro player ou de plataforma. README do original não foi usado como prova isolada. Confirmação de comportamento exige execução e testes com uma playlist sintética/servidor controlado, não credenciais privadas.
