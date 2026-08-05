# Identidade visual

Coloque os arquivos da marca aqui. O `prepare-beta` leva o ícone para o lugar certo do proxy automaticamente.

## O que já está aqui

| Arquivo | O que é |
|---|---|
| `server-icon.png` | Ícone da lista de servidores, 64 × 64, pronto para uso |
| `icon-512.png` | O mesmo escudo em 512 × 512, para Discord |
| `logo-1024.png` | Logo completo, para site, banner e divulgação |

O ícone é o **escudo recortado do logo original**, não um desenho novo. O entorno — espadas, portal, bandeiras e grama — foi mascarado porque em 64 pixels ele vira ruído colorido em volta do "GC" e derruba a legibilidade justamente onde ela mais importa.

O recorte é reproduzível por [`scripts/extract-shield.ps1`](../../scripts/extract-shield.ps1), que guarda as coordenadas da região do escudo e o contorno usado como máscara:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\extract-shield.ps1 "caminho\do\Logo final.png" .\infra\branding
```

O arquivo original de 4096 × 4096 fica fora do Git de propósito: são 26 MB, e repositório não é lugar para guardar arquivo de arte pesado. Mantenha-o no seu backup.

Existe também [`scripts/make-icon.ps1`](../../scripts/make-icon.ps1), uma versão do escudo desenhada em pixel art a partir do zero. Ficou como alternativa: rende uma borda mais dura em telas pequenas, mas não é a arte oficial da marca.

## server-icon.png — obrigatório 64×64

É o ícone que aparece na lista de servidores do Minecraft Java. O protocolo aceita **exatamente 64 × 64 pixels, PNG**. Fora dessa medida o Velocity ignora o arquivo e o servidor aparece sem ícone.

Não use o logo completo reduzido. Elementos pequenos — bandeiras, detalhes de fundo, brilho — viram manchas de poucos pixels e só produzem ruído. **Faça um recorte:** o escudo com as iniciais ocupando quase todo o quadro, sem os elementos periféricos.

O teste é simples: reduza para 64 px, olhe de longe e veja se ainda dá para dizer que servidor é. Se não der, corte mais.

## Outros formatos

| Arquivo | Uso | Tamanho |
|---|---|---|
| `server-icon.png` | Lista de servidores Java | 64 × 64 |
| `logo.png` | Discord, site, divulgação | 512 × 512 ou maior |
| `banner.png` | Topo do Discord, redes | proporção larga |

O Bedrock não exibe ícone de servidor externo na lista; isso vale só para o Java.

## Um aviso que vale para todo material

Usar estética de blocos é livre, mas a marca Minecraft não é sua. Não use o logotipo do Minecraft, não sugira que o servidor é oficial, e mantenha o aviso de não afiliação onde houver loja ou divulgação:

```text
NOT AN OFFICIAL MINECRAFT PRODUCT.
NOT APPROVED BY OR ASSOCIATED WITH MOJANG OR MICROSOFT.
```

Detalhes em [MONETIZACAO.md](../../docs/MONETIZACAO.md).
