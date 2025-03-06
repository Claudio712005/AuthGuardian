INSERT INTO perfil (id, nome, descricao)
VALUES (1, 'ADMIN', 'Administrador'),
       (2, 'GERENTE', 'Gerente do sistema'),
       (3, 'USUARIO', 'Usuário padrão'),
       (4, 'SUPORTE', 'Equipe de suporte');

INSERT INTO role (id, nome, descricao)
VALUES (1, 'ROLE_ADMIN', 'Administrador'),
       (2, 'ROLE_GERENTE', 'Gerente do sistema'),
       (3, 'ROLE_USUARIO', 'Usuário padrão'),
       (4, 'ROLE_SUPORTE', 'Equipe de suporte');

INSERT INTO usuario (id, nome, email, senha)
VALUES (1, 'Cláudio', 'clausilvaaraujo11@gmail.com', '$2b$12$4FM3A0un93R72ieiEddIE.J9hWbrO64j93W4cJZy0jyQcQo2WMFBC'),
       (2, 'Mariana', 'mariana@gmail.com', '$2b$12$4FM3A0un93R72ieiEddIE.J9hWbrO64j93W4cJZy0jyQcQo2WMFBC'),
       (3, 'Rafael', 'rafael@gmail.com', '$2b$12$4FM3A0un93R72ieiEddIE.J9hWbrO64j93W4cJZy0jyQcQo2WMFBC'),
       (4, 'Carla', 'carla@gmail.com', '$2b$12$4FM3A0un93R72ieiEddIE.J9hWbrO64j93W4cJZy0jyQcQo2WMFBC');

INSERT INTO perfil_role (id, perfil_id, role_id)
VALUES (1, 1, 1),
       (2, 2, 2),
       (3, 3, 3),
       (4, 4, 4),
       (5, 2, 3),
       (6, 1, 2),
       (7, 1, 3),
       (8, 1, 4);

INSERT INTO usuario_perfil (id, usuario_id, perfil_id)
VALUES (1, 1, 1),
       (2, 2, 2),
       (3, 3, 3),
       (4, 4, 4),
       (5, 2, 3),
       (6, 1, 2),
       (7, 1, 3),
       (8, 1, 4);
