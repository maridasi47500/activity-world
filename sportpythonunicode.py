import unicodedata

# Define the Unicode range for Miscellaneous Symbols and Pictographs
start = 127939
end = 129349

# Keywords to filter sport-related emojis
keywords = [
"swim", "swimming",
    "sport", "medal", "trophy", "ball", "football", "basketball", "tennis",
    "volleyball", "rugby", "cricket", "ping pong", "badminton", "hockey",
    "ski", "snowboard", "skate", "curling", "wrestling", "swimming", "surfing",
    "rowing", "biking", "running", "lifting", "golf", "archery", "karate",
    "martial arts", "water polo", "handball", "juggling", "fencing"
]

print("Emoji | Decimal | HTML Code | Name")
print("---------------------------------------------")

for codepoint in range(start, end + 1):
    char = chr(codepoint)
    name = unicodedata.name(char, "").lower()
    if any(keyword in name for keyword in keywords):
        html = f"&#{codepoint};"
        #print(f"{char}     | {codepoint}     | {html} | {name}")
        print(f"<option value=\"{html}\">{html} {name}</option>")

