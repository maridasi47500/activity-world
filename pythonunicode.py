# Emoji range: U+1F3A0 to U+1F3FF (decimal 127968 to 128255)

start = 127968
end = 128255

print("Emoji | Decimal Code | HTML Entity")
print("------------------------------------")

for codepoint in range(start, end + 1):
    try:
        emoji = chr(codepoint)
        html_entity = f"&#{codepoint};"
        print(f"{emoji}     | {codepoint}        | {html_entity}")
    except:
        continue  # Skip invalid codepoints

