import csv
import json

with open("users.csv", "r", encoding="utf-8") as f:
    reader = csv.reader(f)
    headers = next(reader)
    users = []
    for idx, row in enumerate(reader):
        if not row:
            continue
        email = (row[1] or row[6] or "").strip()
        first_name = (row[2] or "").strip()
        middle_name = (row[3] or "").strip()
        last_name = (row[4] or "").strip()
        branch = (row[8] or "").strip()
        city = (row[15] or "").strip()
        skills = (row[45] or "").strip()
        phone = (row[37] or "").strip()
        
        if email:
            users.append({
                "idx": idx + 1,
                "email": email,
                "firstName": first_name,
                "lastName": last_name or middle_name,
                "branch": branch,
                "city": city,
                "phone": phone,
                "skills": skills
            })

print(f"Total users: {len(users)}")
for u in users[:10]:
    print(f"[{u['idx']}] {u['firstName']} {u['lastName']} <{u['email']}> - {u['branch']} - {u['city']}")

