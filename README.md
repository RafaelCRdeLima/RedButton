# Botão Vermelho

Botão de emergência para o celular Android de uma pessoa idosa. Um toque no botão vermelho:

1. **manda um SMS** para o número configurado: "EMERGÊNCIA! Mãe apertou o botão vermelho e
   está ligando para você.", com o link do Google Maps da última posição conhecida;
2. **liga** para o mesmo número, logo em seguida, sem esperar;
3. se aquela posição for velha (mais de 2 min) ou imprecisa (pior que 50 m), busca a atual
   por até 45 s, com a ligação já em andamento, e manda **um segundo SMS** com ela.

O botão fica em dois lugares: na tela do app (um círculo vermelho que ocupa quase toda a tela)
e num **widget da tela inicial**, que dispara com um toque, sem abrir o app.

Não usa internet nem serviços do Google, só SMS e ligação comuns. Funciona do Android 6 em diante.

## Baixar

**[⬇ Baixar BotaoVermelho.apk](https://github.com/RafaelCRdeLima/RedButton/releases/latest/download/BotaoVermelho.apk)**

Ou aponte a câmera do celular para o código abaixo:

<img src="docs/qr-baixar.svg" alt="QR code para baixar o APK" width="220">

## Instalar

### Pelo próprio celular

1. Abra o link acima (ou escaneie o QR) **no celular onde o botão vai ficar**. O arquivo
   `BotaoVermelho.apk` é baixado.
2. Toque no arquivo baixado, na notificação ou na pasta Downloads.
3. Se o Android pedir, permita **"instalar apps desta fonte"** para o navegador e volte.
4. Toque em **Instalar**. Se o Play Protect avisar, toque em **Mais detalhes → Instalar mesmo
   assim**. O aviso aparece porque o app não vem da Play Store.

No Brasil, desde 30/09/2026, o Android 8+ tem a verificação de desenvolvedor. Se a instalação
for recusada por "desenvolvedor não verificado", use o cabo (abaixo). Também dá para ligar o
"fluxo avançado" nas opções do desenvolvedor, mas ele exige uma espera de 24 h.

### Pelo cabo USB (sempre funciona)

1. No celular: Configurações → Sobre o telefone → toque 7 vezes em "Número da versão"; depois
   Opções do desenvolvedor → ligue a "Depuração USB".
2. Ligue o cabo no computador, aceite o aviso no celular e rode:
   `adb install -r BotaoVermelho.apk`
3. Se quiser, desligue a depuração USB depois.

Pelo cabo, o Android também não marca o SMS como "configuração restrita" (veja abaixo).

## Configurar (uma vez)

Quem instala é que faz isto, não a pessoa idosa:

1. Abra o app. Na primeira vez, ele já vai para a configuração. Nas outras, **segure o dedo**
   em "Configurações", embaixo do botão.
2. Preencha o nome e o número com DDD de quem vai receber o pedido de ajuda (vira +55
   automaticamente), e como ela aparece na mensagem (ex.: "Mãe").
3. **Salvar e dar as permissões**: aceite SMS e ligação. Na localização, escolha **"Durante o
   uso do app"**, e não "Só esta vez", que se perde ao fechar o app.
4. **Não remover as permissões se não usar** (Android 11+): desligue a opção "Pausar atividade
   no app se não for usado" (o nome muda conforme a marca). Isso é importante: sem esse passo,
   depois de alguns meses sem uso o Android tira as permissões sozinho e o botão para de mandar
   o SMS.
5. **Testar só a mensagem**: deve chegar um SMS marcado como TESTE.
6. **Pôr o botão na tela inicial**: põe o widget, que você pode aumentar segurando o dedo nele.
7. Teste de verdade uma vez, apertando o botão: deve chegar o SMS e o telefone deve tocar.

A lista com ✔/✘ na tela de configuração mostra o que falta.

### Se a permissão de SMS vier bloqueada

Em Android 13+ (e principalmente 15+), app instalado por arquivo pode ter o SMS marcado como
"configuração restrita": o pedido de permissão nem aparece. Resolva assim: **Abrir permissões do
app** → menu ⋮ no canto superior → **Permitir configurações restritas** → confirme → volte e dê a
permissão de SMS. A instalação pelo cabo evita isso.

## Limites que convém saber

- Com o celular **bloqueado**, ela precisa desbloquear antes de tocar no widget. Como reforço, o
  próprio Android tem o "SOS de emergência" (apertar o botão de ligar 5 vezes). Na Samsung, ele
  também manda SMS para contatos de emergência.
- **Dois chips**: escolha um chip padrão para ligações e para SMS, senão o Android pergunta qual
  usar.
- O SMS precisa de sinal de celular e de crédito ou plano com SMS. Se a mensagem falhar, a
  ligação acontece mesmo assim.
- Sem a permissão de ligação, o app abre o discador com o número já digitado, e ela precisa
  apertar o botão verde.
- A ligação é normal, não no viva-voz: o Android não deixa um app comum ligar o viva-voz numa
  chamada.
- Dois toques em menos de 10 s contam como um só, para não ligar duas vezes.

## Para desenvolvedores

### Compilar

Abra a pasta no Android Studio, ou rode `./compilar.sh`, que gera o `BotaoVermelho.apk` na
pasta. O script procura o JDK 17 em `~/Android/jdk-17.0.21.1+2` (ou use `JAVA_HOME_17`) e o
SDK em `~/Android/Sdk` (ou use `ANDROID_HOME`). O app não tem dependências: usa só a API do
próprio Android.

O APK dos releases é assinado com a chave de debug de quem compilou
(`~/.android/debug.keystore`). Para **atualizar** o app num celular sem desinstalar, a versão nova
precisa ser assinada com a mesma chave. Com outra chave, o Android recusa a atualização: é preciso
desinstalar e configurar de novo.

### Publicar uma versão nova

Aumente `versionCode` e `versionName` em `app/build.gradle.kts`, rode `./compilar.sh` e:

```bash
gh release create v1.1 BotaoVermelho.apk --title "Botão Vermelho 1.1" --notes "O que mudou"
```

O link e o QR code acima apontam sempre para o release mais recente.

### Código

`app/src/main/java/br/botaovermelho/`:

| arquivo | o quê |
|---|---|
| `Emergency.kt` | o que o botão faz: SMS, depois ligação, texto das mensagens |
| `LocationService.kt` | serviço em primeiro plano que busca a posição atual e manda o 2º SMS |
| `Locator.kt` | última posição conhecida e critério de "boa o bastante" |
| `MainActivity.kt` | a tela com o botão |
| `SettingsActivity.kt` | configuração, permissões, teste e widget |
| `SosWidget.kt`, `TriggerActivity.kt` | o widget da tela inicial |
| `Config.kt` | dados salvos e normalização do número (+55) |
