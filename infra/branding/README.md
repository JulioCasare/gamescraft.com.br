# Identidade visual

Coloque os arquivos da marca aqui. O `prepare-beta` leva o ícone para o lugar certo do proxy automaticamente.

## O que já está aqui

| Arquivo | O que é |
|---|---|
| `server-icon.png` | Ícone da lista de servidores, 64 × 64, pronto para uso |
| `icon-512.png` | A mesma arte em 512 × 512, para Discord e divulgação |

São uma redução do logo completo a só o escudo com as iniciais — o que sobrevive em 64 pixels. Ambos saem de [`scripts/make-icon.ps1`](../../scripts/make-icon.ps1), que desenha a arte numa grade lógica de 32 × 32 e amplia por múltiplo inteiro, mantendo a borda dura do pixel art.

Para mudar cor, letra ou formato:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\make-icon.ps1 .\infra\branding
```

Substitua os arquivos à vontade se tiver uma versão feita à mão — o script é conveniência, não obrigação.

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
