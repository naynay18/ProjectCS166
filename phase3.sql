--query 1
INSERT INTO Doctor
Select 251, 'Lady Gaga', 'Allergist', 23
where not exists(select* from Doctor where doctor_ID = 251);

--query 2
Insert into Patient
Select 251, 'Abel Weeknd', 'M', 48, '1234 Season Street', 4
where not exists(select* from Patient where patient_ID = 251);

--query 3
insert into Appointment
Select 550, '1/13/2020', '8:00-10:00', 'PA'
where not exists(select* from Appointment where appnt_ID = 550);

--query 4
INSERT INTO Patient
SELECT 252, 'John', 'M', 48, '4763 Lemon', 3
WHERE NOT EXISTS(SELECT* from Patient where patient_ID = 252);

SELECT*
FROM Patient
WHERE patient_ID = 252;

UPDATE Patient
SET number_of_appts = number_of_appts + 1
WHERE patient_ID = 252 AND EXISTS
(SELECT*
FROM Patient P
WHERE EXISTS(Select* from Patient P2 where P2.patient_ID = P.patient_ID));

SELECT*
FROM Patient
WHERE patient_ID = 252;

--AC -> WL
SELECT*
FROM Appointment
WHERE appnt_ID = 24;
UPDATE Appointment
SET status = 'WL'
WHERE appnt_ID = 24 AND status = 'AC' AND status = (SELECT A.status
FROM Appointment A, has_appointment HA, Doctor Dr
WHERE A.status = 'AC' AND A.appnt_ID = 24 AND HA.appt_id = 24 AND HA.doctor_id = 70 AND Dr.doctor_ID = 70);
SELECT*
FROM Appointment
WHERE appnt_ID = 24;

--AV -> AC
SELECT*
FROM Appointment
WHERE appnt_ID = 26;
UPDATE Appointment
SET status = 'AC'
WHERE appnt_ID = 26 AND status = 'AV' AND status = (SELECT A.status
FROM Appointment A, has_appointment HA, Doctor Dr
WHERE A.status = 'AV' AND A.appnt_ID = 26 AND HA.appt_id = 26 AND HA.doctor_id = 88 AND Dr.doctor_ID = 88);
SELECT*
FROM Appointment A
WHERE A.appnt_ID = 26;

--query 5
SELECT Appt.appnt_id, Appt.adate, Appt.time_slot, Appt.status
FROM Doctor Dr, Appointment Appt, has_appointment has_appt
WHERE Dr.doctor_ID = 118 AND has_appt.doctor_id = Dr.doctor_ID AND has_appt.appt_id = Appt.appnt_ID
        AND (Appt.status = 'AC' OR Appt.status = 'AV')
        AND (Appt.adate BETWEEN '9/3/21' AND '9/29/21');


--query 6
SELECT A.status, A.adate, A.appnt_ID
FROM Appointment A, has_appointment HA
WHERE A.appnt_ID = HA.appt_id AND A.status = 'AV' AND A.adate = '9/7/21' AND
  EXISTS (SELECT*
  FROM Doctor DR
  WHERE DR.doctor_ID = HA.doctor_ID
  AND EXISTS (SELECT D.name
                FROM Department D
                WHERE D.dept_ID = DR.did AND D.name = 'Oncology'));
                
--query 7
SELECT DR.name,A.status, COUNT(appnt_id)
FROM  Doctor DR, Appointment A, has_appointment HA
WHERE  HA.doctor_ID = DR.doctor_ID AND HA.appt_ID = A.appnt_ID AND
EXISTS (SELECT COUNT(A1.status) AS differentTypes
        FROM Appointment A1, Appointment A2
        WHERE A1.appnt_ID =A2.appnt_ID
        ORDER BY differentTypes)
GROUP BY DR.name, A.status
ORDER BY COUNT(appnt_id) desc;


--query 8
SELECT DR1.name, A.status, COUNT(P.patient_ID)
FROM Patient P, Searches S, Appointment A, Doctor DR1, has_appointment HA
WHERE P.patient_ID = S.pid AND A.appnt_ID = S.aid AND A.status = 'AV' AND DR1.doctor_ID = HA.doctor_id
GROUP BY DR1.name, A.status;
