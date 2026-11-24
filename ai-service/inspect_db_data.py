import pymysql

conn = pymysql.connect(
    host='gateway01.ap-northeast-1.prod.aws.tidbcloud.com',
    port=4000,
    user='4HfiL5GrXTF1yhA.root',
    password='L5MuaILLvqbXplME',
    database='test',
    ssl={'ssl': True}
)
cursor = conn.cursor()
cursor.execute("""
    SELECT u.id, u.first_name, u.last_name, u.email, u.primary_role, 
           GROUP_CONCAT(s.name SEPARATOR ', ') as skills
    FROM users u
    LEFT JOIN user_skills us ON u.id = us.user_id
    LEFT JOIN skills s ON us.skill_id = s.id
    GROUP BY u.id, u.first_name, u.last_name, u.email, u.primary_role
""")
rows = cursor.fetchall()
print(f"Total users found: {len(rows)}")
for r in rows[:10]:
    print(f"User ID={r[0]}: {r[1]} {r[2]} ({r[3]}) | Role: {r[4]} | Skills: {r[5]}")

cursor.execute("SELECT count(*) FROM projects")
print(f"Total projects: {cursor.fetchone()[0]}")

cursor.execute("SELECT count(*) FROM hackathons")
print(f"Total hackathons: {cursor.fetchone()[0]}")

conn.close()
