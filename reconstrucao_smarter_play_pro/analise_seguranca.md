# Análise de segurança

Manifesto solicita rede, boot, armazenamento legado, microfone, overlay, cache, foreground service e wake lock, além de Firebase/billing. APK declara `usesCleartextTraffic=true` e inclui serviços/bibliotecas OpenVPN. Isso não demonstra uso efetivo. Sem call-sites completos e tráfego dinâmico, o propósito dos componentes/hosts é parcial ou hipótese. Hosts estão detalhados na especificação; a URL HTTP `upgrade.php` aparenta atualização pelo nome do caminho.

A reconstrução usará identidade e pacote próprios; credenciais ficam locais e só seguem para o host IPTV digitado pelo usuário. M3U local via Storage Access Framework; acesso remoto somente por ação explícita. Sem boot, VPN, push, overlay, billing, telemetria ou execução arbitrária. Não copiar código, marca, imagens ou fontes proprietárias.
