# MISSÃO DO CODEX — RECONSTRUÇÃO AUTOMATIZADA ANDROID

## OBJETIVO

Assumir o ReaDroid como agente técnico principal e preparar um fluxo automatizado para reconstruir um aplicativo Android independente a partir de um APK de referência.

O objetivo final é entregar um APK instalável e funcional, reproduzindo, tanto quanto possível, as funcionalidades, telas, navegação e comportamentos observáveis do aplicativo de referência.

Priorizar Python/Kivy se a auditoria confirmar que essa abordagem é viável no ambiente disponível.

## 1. PRINCÍPIOS

- Trabalhe autonomamente, sem exigir que o usuário programe ou conduza a arquitetura.
- Evite solicitar cópias manuais de arquivos, XMLs e relatórios.
- Reaproveite o que já funciona no ReaDroid.
- Não crie ferramentas, módulos ou camadas desnecessárias.
- Não transforme o desenvolvimento do ReaDroid em um objetivo independente.
- Priorize o caminho mais curto até um aplicativo funcional e instalável.
- Preserve arquivos, alterações existentes e backups.
- Não use operações destrutivas como git reset, git checkout ou git clean.
- Não envie arquivos privados ou dados do APK para serviços externos sem autorização expressa.
- Não copie código-fonte proprietário, ativos protegidos ou identidade visual proprietária.

## 2. PRIMEIRA ETAPA: AUDITORIA

Nesta primeira execução, examine somente o projeto ReaDroid.

Identifique:
- Estrutura e funcionalidades existentes.
- Estado do analisador e reconstrutor.
- Especificação funcional já implementada.
- Testes disponíveis e seus resultados.
- Ferramentas e dependências instaladas.
- Limitações reais do Termux e do Android.
- Viabilidade de implementar e compilar um APK independente.

Preserve o trabalho existente. Não reescreva componentes funcionais sem justificativa.

## 3. FLUXO DE RECONSTRUÇÃO A PREPARAR

Planeje uma execução automatizada capaz de:

1. Receber o caminho do APK de referência.
2. Analisar manifesto, recursos, telas e componentes.
3. Investigar navegação e comportamentos observáveis.
4. Distinguir evidências confirmadas de hipóteses.
5. Produzir especificação funcional sanitizada.
6. Implementar um aplicativo independente.
7. Executar testes e identificar falhas.
8. Corrigir problemas encontrados.
9. Compilar e validar o APK final.

O usuário não deve precisar executar manualmente cada etapa intermediária.

## 4. RECONSTRUÇÃO

Reproduza, conforme as evidências disponíveis:
- Funcionalidades.
- Telas e navegação.
- Entradas e saídas.
- Persistência de dados.
- Estados e mensagens funcionais.
- Integrações necessárias.
- Comportamentos observáveis.

Não declare funcionalidades implementadas sem desenvolvimento e testes efetivos.

## 5. ENTREGA

A meta final é entregar:
- Código-fonte independente.
- APK compilado e instalável.
- Testes executados.
- Relatório objetivo das funcionalidades implementadas.
- Limitações conhecidas.

Compilar não basta: verificar inicialização e funcionamento do aplicativo.

## 6. AUTORIZAÇÃO E LIMITES DA PRIMEIRA EXECUÇÃO

IMPORTANTE: nesta primeira execução:

- Faça somente a auditoria do projeto.
- NÃO analise o APK privado.
- NÃO inicie compilação ou assinatura.
- NÃO instale nem remova pacotes.
- NÃO altere arquivos do projeto.
- NÃO execute operações externas com dados privados.
- NÃO modifique menus, banners ou componentes existentes.

Ao terminar, apresente um diagnóstico baseado no estado real do projeto e um plano técnico objetivo para chegar à reconstrução e à entrega do APK.

Aguarde autorização expressa antes de analisar o APK ou modificar qualquer arquivo.

## 7. CRITÉRIO DE SUCESSO

O sucesso não é criar mais ferramentas ou relatórios.

O sucesso é entregar um aplicativo independente, funcional e instalável, com a maior equivalência possível ao comportamento observável do APK de referência.
