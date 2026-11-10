# Especificação funcional (análise estática)

Referência: `iptv-smarters-pro-3-1-5-1.apk`, pacote `com.nst.iptvsmarterstvbox`, versão 3.1.5.1, min SDK 17, target SDK 30. SHA-256 `81fa604fadc64869ea8239e0366178c50d12b80db88f40b1a4b56369ae495ff4`. Não instalado nem executado. Evidências: manifesto binário, nomes DEX, strings e esquemas SQL. Presença de classe/string não prova execução ou conectividade.

## Funcionalidades e evidência

- Login/contas: `LoginActivity`, `AccountInfoActivity`, modelos Xtream e tabelas `multi_user`/`multi_user_m3u` (forte para fluxo de conta; autenticação não testada).
- Xtream: callbacks de painel, modelos de streams e tabelas específicas de live, VOD, séries, categorias, favoritos, EPG e retomada (forte para integração; endpoints/protocolo exato pendente de call-site).
- M3U: mensagem “Please select .m3u File”, extensão M3U e tabelas `_m3u` (forte para importação; URL remota não provada).
- Live/VOD/séries: activities, modelos e tabelas específicas (forte).
- Player: activities interna/externa e dependências ExoPlayer/IJK/FFmpeg (forte; controles pendentes).
- EPG/XMLTV: atividades de importação/grade, callback XMLTV e tabelas EPG/fontes (forte).
- Favoritos/histórico: tabelas de favoritos, live visto, recentes VOD/séries e retomada (forte).
- Backup/restore: `BackupAndRestoreActivity` presente (existência confirmada; operação pendente).
- OpenVPN: classes, serviços, bibliotecas e executáveis multi-ABI presentes. Não prova uso efetivo nem conexão automática.
- Push: `MyFirebaseMessagingService` e `MyFirebaseInstanceIDService` presentes; payload e uso não determinados.

## Hosts observados somente como strings DEX

`http://51.158.145.183/` (função desconhecida); `http://51.75.16.104/upgrade.php?type=package&id=` (aparente atualização, hipótese pelo path); `http://api-android.whmcssmarters.com/` (WHMCS/licenciamento, hipótese pelo nome); `https://cms.alldrama.tv/` e rota `viewinvoice.php` (CMS/faturamento, hipótese); `https://users.iptvsmarters.com/`, carrinho e reset de senha (portal de usuário, hipótese pelas rotas). String não comprova requisição, DNS, resposta ou envio de credenciais. Nenhum host de terceiros será usado na reconstrução.

## Escopo independente e limites

Pacote/identidade próprios; importação M3U local, conexão Xtream apenas ao host fornecido pelo usuário, navegação live/filmes/séries, busca, favoritos, player real e persistência, conforme implementado e testado. Sem boot, telemetria, VPN, push ou endpoints de terceiros. Não houve execução dinâmica, captura de tráfego ou comparação visual. Variações do protocolo e estados de erro permanecem pendentes de confirmação.
