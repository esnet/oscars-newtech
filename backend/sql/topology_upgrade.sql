DROP TABLE device CASCADE;
DROP TABLE device_capabilities CASCADE;
DROP TABLE device_ports CASCADE;
DROP TABLE device_reservable_vlans CASCADE;
DROP TABLE port CASCADE;
DROP TABLE port_adjcy CASCADE;
DROP TABLE port_adjcy_metrics CASCADE;
DROP TABLE port_capabilities CASCADE;
DROP TABLE port_reservable_vlans CASCADE;
DROP TABLE version CASCADE;



CREATE TABLE public.adjcy (
    id bigint NOT NULL,
    a_id bigint,
    z_id bigint
);

ALTER TABLE public.adjcy OWNER TO oscars;

CREATE TABLE public.adjcy_metrics (
    adjcy_id bigint NOT NULL,
    metrics bigint,
    metrics_key integer NOT NULL
);

ALTER TABLE public.adjcy_metrics OWNER TO oscars;

CREATE TABLE public.device (
                               id bigint NOT NULL,
                               ipv4address character varying(255),
                               ipv6address character varying(255),
                               latitude double precision,
                               location character varying(255),
                               location_id integer,
                               longitude double precision,
                               model integer,
                               type integer,
                               urn character varying(255)
);

ALTER TABLE public.device OWNER TO oscars;

CREATE TABLE public.device_capabilities (
                                            device_id bigint NOT NULL,
                                            capabilities integer
);


ALTER TABLE public.device_capabilities OWNER TO oscars;


CREATE TABLE public.device_ports (
                                     device_id bigint NOT NULL,
                                     ports_id bigint NOT NULL
);


ALTER TABLE public.device_ports OWNER TO oscars;


CREATE TABLE public.device_reservable_vlans (
                                                device_id bigint NOT NULL,
                                                ceiling integer,
                                                floor integer
);

ALTER TABLE public.device_reservable_vlans OWNER TO oscars;

CREATE TABLE public.point (
                              id bigint NOT NULL,
                              addr character varying(255),
                              device character varying(255),
                              ifce character varying(255),
                              port character varying(255)
);


ALTER TABLE public.point OWNER TO oscars;

CREATE TABLE public.port (
                             id bigint NOT NULL,
                             reservable_egress_bw integer,
                             reservable_ingress_bw integer,
                             tags bytea,
                             urn character varying(255),
                             device_id bigint
);

ALTER TABLE public.port OWNER TO oscars;

CREATE TABLE public.port_capabilities (
                                          port_id bigint NOT NULL,
                                          capabilities integer
);


ALTER TABLE public.port_capabilities OWNER TO oscars;


CREATE TABLE public.port_ifces (
                                   port_id bigint NOT NULL,
                                   ipv4address character varying(255),
                                   ipv6address character varying(255),
                                   port character varying(255),
                                   urn character varying(255)
);

ALTER TABLE public.port_ifces OWNER TO oscars;


CREATE TABLE public.port_reservable_vlans (
                                              port_id bigint NOT NULL,
                                              ceiling integer,
                                              floor integer
);


ALTER TABLE public.port_reservable_vlans OWNER TO oscars;


CREATE TABLE public.version (
                                id bigint NOT NULL,
                                updated timestamp without time zone,
                                valid boolean
);


ALTER TABLE public.version OWNER TO oscars;



ALTER TABLE ONLY public.adjcy_metrics
    ADD CONSTRAINT adjcy_metrics_pkey PRIMARY KEY (adjcy_id, metrics_key);

ALTER TABLE ONLY public.adjcy
    ADD CONSTRAINT adjcy_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.device
    ADD CONSTRAINT device_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.device_ports
    ADD CONSTRAINT device_ports_pkey PRIMARY KEY (device_id, ports_id);

ALTER TABLE ONLY public.point
    ADD CONSTRAINT point_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.port
    ADD CONSTRAINT port_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.port
    ADD CONSTRAINT uk_24jj2vjdky6xs94w12oo3hh6j UNIQUE (urn);

ALTER TABLE ONLY public.device_ports
    ADD CONSTRAINT uk_7l2uyp0mg028hq3wqdw8adul UNIQUE (ports_id);
ALTER TABLE ONLY public.device
    ADD CONSTRAINT uk_g9hp5d15ay6ed7fu5c2qs44tx UNIQUE (urn);
ALTER TABLE ONLY public.version
    ADD CONSTRAINT version_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.port_ifces
    ADD CONSTRAINT fkc8mr22gn7q9rudps873giwxqm FOREIGN KEY (port_id) REFERENCES public.port(id);
ALTER TABLE ONLY public.device_reservable_vlans
    ADD CONSTRAINT fkdlvowg0jd6b790ja3cryv9rtb FOREIGN KEY (device_id) REFERENCES public.device(id);
ALTER TABLE ONLY public.port_reservable_vlans
    ADD CONSTRAINT fkgbt4ulvh9vectjga9uom6d17 FOREIGN KEY (port_id) REFERENCES public.port(id);

ALTER TABLE ONLY public.adjcy
    ADD CONSTRAINT fkh186iim9sv0f7ak259sramhsj FOREIGN KEY (a_id) REFERENCES public.point(id);

ALTER TABLE ONLY public.device_capabilities
    ADD CONSTRAINT fkhl5k4w984c88p08xgoh1h7891 FOREIGN KEY (device_id) REFERENCES public.device(id);

ALTER TABLE ONLY public.port_capabilities
    ADD CONSTRAINT fkia6tkai22vmyt7d9iimfv6c6 FOREIGN KEY (port_id) REFERENCES public.port(id);

ALTER TABLE ONLY public.device_ports
    ADD CONSTRAINT fkj54bcd7wi9st57a0il3n3i8c8 FOREIGN KEY (ports_id) REFERENCES public.port(id);

ALTER TABLE ONLY public.port
    ADD CONSTRAINT fkkf8semrjdomxgnhbq6l979chu FOREIGN KEY (device_id) REFERENCES public.device(id);

ALTER TABLE ONLY public.adjcy_metrics
    ADD CONSTRAINT fklo2ha3pypav4h7sb1o1xe3equ FOREIGN KEY (adjcy_id) REFERENCES public.adjcy(id);


ALTER TABLE ONLY public.adjcy
    ADD CONSTRAINT fknfhdvvdnsf0l0hvxggn3goi6o FOREIGN KEY (z_id) REFERENCES public.point(id);
ALTER TABLE ONLY public.device_ports
    ADD CONSTRAINT fkqtqfgllr6rhqv0v5wwbmb95oy FOREIGN KEY (device_id) REFERENCES public.device(id);

