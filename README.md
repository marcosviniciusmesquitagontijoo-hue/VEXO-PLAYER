# Vexo Player — projeto inicial Android

Projeto Android Studio em Kotlin + Jetpack Compose.

## Incluído
- Tela de login com usuário e senha.
- Servidor padrão `http://agenciacinepoa.top`, invisível na tela de login.
- Configurações para trocar/restaurar servidor.
- Validação inicial via endpoint compatível com Xtream `player_api.php`.
- Tela inicial com TV ao vivo, Filmes, Séries, Favoritos, Configurações e Recarregar.
- Tema escuro com verde.

## Abrir
1. Instale Android Studio atual.
2. Abra a pasta `VexoPlayer`.
3. Aguarde a sincronização Gradle.
4. Para gerar APK: **Build > Build Bundle(s) / APK(s) > Build APK(s)**.

## Limitações importantes
Este é um projeto inicial de interface e autenticação, não um player completo ainda. As categorias, favoritos e reprodução de streams ainda precisam ser implementados e testados contra uma API Xtream autorizada. A URL padrão foi definida conforme solicitado, mas não foi possível verificar daqui se o servidor está ativo ou se usa exatamente esse protocolo/API. O manifesto permite HTTP para compatibilidade com o endereço informado; para distribuição pública, prefira HTTPS e revise as políticas de segurança e da Google Play.


## Ícone Vexo Player
O projeto agora inclui a nova arte VEXO PLAYER nos recursos `mipmap-*` do Android e a referência do ícone no `AndroidManifest.xml`. Ao compilar um novo APK, o launcher usará este ícone.
