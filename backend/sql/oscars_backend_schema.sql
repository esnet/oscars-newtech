--
-- PostgreSQL database dump
--

-- Dumped from database version 9.6.2
-- Dumped by pg_dump version 9.6.2

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: archived; Type: TABLE; Schema: public; Owner: oscars
--

CREATE TABLE archived (
    id bigint NOT NULL,
    connection_id character varying(255),
    cmp_id bigint,
    schedule_id bigint
);


ALTER TABLE archived OWNER TO oscars;

--
-- Name: command_param; Type: TABLE; Schema: public; Owner: oscars
--

CREATE TABLE command_param (
    id bigint NOT NULL,
    connection_id character varying(255),
    param_type integer,
    ref_id character varying(255),
    resource integer,
    urn character varying(255),
    schedule_id bigint
);


ALTER TABLE command_param OWNER TO oscars;

--
-- Name: components; Type: TABLE; Schema: public; Owner: oscars
--

CREATE TABLE components (
    id bigint NOT NULL
);


ALTER TABLE components OWNER TO oscars;

--
-- Name: components_fixtures; Type: TABLE; Schema: public; Owner: oscars
--

CREATE TABLE components_fixtures (
    components_id bigint NOT NULL,
    fixtures_id bigint NOT NULL
);


ALTER TABLE components_fixtures OWNER TO oscars;

--
-- Name: components_junctions; Type: TABLE; Schema: public; Owner: oscars
--

CREATE TABLE components_junctions (
    components_id bigint NOT NULL,
    junctions_id bigint NOT NULL
);


ALTER TABLE components_junctions OWNER TO oscars;

--
-- Name: components_pipes; Type: TABLE; Schema: public; Owner: oscars
--

CREATE TABLE components_pipes (
    components_id bigint NOT NULL,
    pipes_id bigint NOT NULL
);


ALTER TABLE components_pipes OWNER TO oscars;

--
-- Name: connection; Type: TABLE; Schema: public; Owner: oscars
--

CREATE TABLE connection (
    id bigint NOT NULL,
    connection_id character varying(255),
    state character varying(255),
    username character varying(255),
    archived_id bigint,
    held_id bigint,
    reserved_id bigint
);


ALTER TABLE connection OWNER TO oscars;

--
-- Name: design; Type: TABLE; Schema: public; Owner: oscars
--

CREATE TABLE design (
    id bigint NOT NULL,
    description character varying(255),
    design_id character varying(255),
    username character varying(255),
    cmp_id bigint
);


ALTER TABLE design OWNER TO oscars;

--
-- Name: device; Type: TABLE; Schema: public; Owner: oscars
--

CREATE TABLE device (
    id bigint NOT NULL,
    ipv4address character varying(255),
    ipv6address character varying(255),
    model integer,
    type integer,
    urn character varying(255)
);


ALTER TABLE device OWNER TO oscars;

--
-- Name: device_capabilities; Type: TABLE; Schema: public; Owner: oscars
--

CREATE TABLE device_capabilities (
    device_id bigint NOT NULL,
    capabilities integer
);


ALTER TABLE device_capabilities OWNER TO oscars;

--
-- Name: device_ports; Type: TABLE; Schema: public; Owner: oscars
--

CREATE TABLE device_ports (
    device_id bigint NOT NULL,
    ports_id bigint NOT NULL
);


ALTER TABLE device_ports OWNER TO oscars;

--
-- Name: device_reservable_vlans; Type: TABLE; Schema: public; Owner: oscars
--

CREATE TABLE device_reservable_vlans (
    device_id bigint NOT NULL,
    ceiling integer,
    floor integer
);


ALTER TABLE device_reservable_vlans OWNER TO oscars;

--
-- Name: ero_hop; Type: TABLE; Schema: public; Owner: oscars
--

CREATE TABLE ero_hop (
    id bigint NOT NULL,
    urn character varying(255)
);


ALTER TABLE ero_hop OWNER TO oscars;

--
-- Name: held; Type: TABLE; Schema: public; Owner: oscars
--

CREATE TABLE held (
    id bigint NOT NULL,
    connection_id character varying(255),
    expiration bytea,
    cmp_id bigint,
    schedule_id bigint
);


ALTER TABLE held OWNER TO oscars;

--
-- Name: hibernate_sequence; Type: SEQUENCE; Schema: public; Owner: oscars
--

CREATE SEQUENCE hibernate_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE hibernate_sequence OWNER TO oscars;

--
-- Name: port; Type: TABLE; Schema: public; Owner: oscars
--

CREATE TABLE port (
    id bigint NOT NULL,
    ipv4address character varying(255),
    ipv6address character varying(255),
    reservable_egress_bw integer,
    reservable_ingress_bw integer,
    tags bytea,
    urn character varying(255),
    device_id bigint
);


ALTER TABLE port OWNER TO oscars;

--
-- Name: port_adjcy; Type: TABLE; Schema: public; Owner: oscars
--

CREATE TABLE port_adjcy (
    id bigint NOT NULL,
    a_id bigint,
    z_id bigint
);


ALTER TABLE port_adjcy OWNER TO oscars;

--
-- Name: port_adjcy_metrics; Type: TABLE; Schema: public; Owner: oscars
--

CREATE TABLE port_adjcy_metrics (
    port_adjcy_id bigint NOT NULL,
    metrics bigint,
    metrics_key integer NOT NULL
);


ALTER TABLE port_adjcy_metrics OWNER TO oscars;

--
-- Name: port_capabilities; Type: TABLE; Schema: public; Owner: oscars
--

CREATE TABLE port_capabilities (
    port_id bigint NOT NULL,
    capabilities integer
);


ALTER TABLE port_capabilities OWNER TO oscars;

--
-- Name: port_reservable_vlans; Type: TABLE; Schema: public; Owner: oscars
--

CREATE TABLE port_reservable_vlans (
    port_id bigint NOT NULL,
    ceiling integer,
    floor integer
);


ALTER TABLE port_reservable_vlans OWNER TO oscars;

--
-- Name: reserved; Type: TABLE; Schema: public; Owner: oscars
--

CREATE TABLE reserved (
    id bigint NOT NULL,
    connection_id character varying(255),
    cmp_id bigint,
    schedule_id bigint
);


ALTER TABLE reserved OWNER TO oscars;

--
-- Name: schedule; Type: TABLE; Schema: public; Owner: oscars
--

CREATE TABLE schedule (
    id bigint NOT NULL,
    beginning bytea,
    connection_id character varying(255),
    ending bytea,
    phase integer,
    ref_id character varying(255)
);


ALTER TABLE schedule OWNER TO oscars;

--
-- Name: tbl_users; Type: TABLE; Schema: public; Owner: oscars
--

CREATE TABLE tbl_users (
    id bigint NOT NULL,
    email character varying(255),
    full_name character varying(255),
    institution character varying(255),
    password character varying(255),
    admin_allowed boolean NOT NULL,
    username character varying(255)
);


ALTER TABLE tbl_users OWNER TO oscars;

--
-- Name: vlan; Type: TABLE; Schema: public; Owner: oscars
--

CREATE TABLE vlan (
    id bigint NOT NULL,
    connection_id character varying(255),
    urn character varying(255),
    vlan_id integer,
    schedule_id bigint
);


ALTER TABLE vlan OWNER TO oscars;

--
-- Name: vlan_fixture; Type: TABLE; Schema: public; Owner: oscars
--

CREATE TABLE vlan_fixture (
    id bigint NOT NULL,
    connection_id character varying(255),
    egress_bandwidth integer,
    eth_fixture_type integer,
    ingress_bandwidth integer,
    port_urn character varying(255),
    junction_id bigint,
    schedule_id bigint,
    vlan_id bigint
);


ALTER TABLE vlan_fixture OWNER TO oscars;

--
-- Name: vlan_fixture_command_params; Type: TABLE; Schema: public; Owner: oscars
--

CREATE TABLE vlan_fixture_command_params (
    vlan_fixture_id bigint NOT NULL,
    command_params_id bigint NOT NULL
);


ALTER TABLE vlan_fixture_command_params OWNER TO oscars;

--
-- Name: vlan_junction; Type: TABLE; Schema: public; Owner: oscars
--

CREATE TABLE vlan_junction (
    id bigint NOT NULL,
    connection_id character varying(255),
    device_urn character varying(255),
    ref_id character varying(255),
    schedule_id bigint,
    vlan_id bigint
);


ALTER TABLE vlan_junction OWNER TO oscars;

--
-- Name: vlan_junction_command_params; Type: TABLE; Schema: public; Owner: oscars
--

CREATE TABLE vlan_junction_command_params (
    vlan_junction_id bigint NOT NULL,
    command_params_id bigint NOT NULL
);


ALTER TABLE vlan_junction_command_params OWNER TO oscars;

--
-- Name: vlan_pipe; Type: TABLE; Schema: public; Owner: oscars
--

CREATE TABLE vlan_pipe (
    id bigint NOT NULL,
    az_bandwidth integer,
    connection_id character varying(255),
    za_bandwidth integer,
    a_id bigint,
    schedule_id bigint,
    z_id bigint
);


ALTER TABLE vlan_pipe OWNER TO oscars;

--
-- Name: vlan_pipe_azero; Type: TABLE; Schema: public; Owner: oscars
--

CREATE TABLE vlan_pipe_azero (
    vlan_pipe_id bigint NOT NULL,
    azero_id bigint NOT NULL
);


ALTER TABLE vlan_pipe_azero OWNER TO oscars;

--
-- Name: vlan_pipe_command_params; Type: TABLE; Schema: public; Owner: oscars
--

CREATE TABLE vlan_pipe_command_params (
    vlan_pipe_id bigint NOT NULL,
    command_params_id bigint NOT NULL
);


ALTER TABLE vlan_pipe_command_params OWNER TO oscars;

--
-- Name: vlan_pipe_zaero; Type: TABLE; Schema: public; Owner: oscars
--

CREATE TABLE vlan_pipe_zaero (
    vlan_pipe_id bigint NOT NULL,
    zaero_id bigint NOT NULL
);


ALTER TABLE vlan_pipe_zaero OWNER TO oscars;

--
-- Name: archived archived_pkey; Type: CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY archived
    ADD CONSTRAINT archived_pkey PRIMARY KEY (id);


--
-- Name: command_param command_param_pkey; Type: CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY command_param
    ADD CONSTRAINT command_param_pkey PRIMARY KEY (id);


--
-- Name: components components_pkey; Type: CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY components
    ADD CONSTRAINT components_pkey PRIMARY KEY (id);


--
-- Name: connection connection_pkey; Type: CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY connection
    ADD CONSTRAINT connection_pkey PRIMARY KEY (id);


--
-- Name: design design_pkey; Type: CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY design
    ADD CONSTRAINT design_pkey PRIMARY KEY (id);


--
-- Name: device device_pkey; Type: CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY device
    ADD CONSTRAINT device_pkey PRIMARY KEY (id);


--
-- Name: device_ports device_ports_pkey; Type: CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY device_ports
    ADD CONSTRAINT device_ports_pkey PRIMARY KEY (device_id, ports_id);


--
-- Name: ero_hop ero_hop_pkey; Type: CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY ero_hop
    ADD CONSTRAINT ero_hop_pkey PRIMARY KEY (id);


--
-- Name: held held_pkey; Type: CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY held
    ADD CONSTRAINT held_pkey PRIMARY KEY (id);


--
-- Name: port_adjcy_metrics port_adjcy_metrics_pkey; Type: CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY port_adjcy_metrics
    ADD CONSTRAINT port_adjcy_metrics_pkey PRIMARY KEY (port_adjcy_id, metrics_key);


--
-- Name: port_adjcy port_adjcy_pkey; Type: CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY port_adjcy
    ADD CONSTRAINT port_adjcy_pkey PRIMARY KEY (id);


--
-- Name: port port_pkey; Type: CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY port
    ADD CONSTRAINT port_pkey PRIMARY KEY (id);


--
-- Name: reserved reserved_pkey; Type: CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY reserved
    ADD CONSTRAINT reserved_pkey PRIMARY KEY (id);


--
-- Name: schedule schedule_pkey; Type: CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY schedule
    ADD CONSTRAINT schedule_pkey PRIMARY KEY (id);


--
-- Name: tbl_users tbl_users_pkey; Type: CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY tbl_users
    ADD CONSTRAINT tbl_users_pkey PRIMARY KEY (id);


--
-- Name: vlan_pipe_command_params uk_14mxv8e8cf1xxf2oqcsot326w; Type: CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY vlan_pipe_command_params
    ADD CONSTRAINT uk_14mxv8e8cf1xxf2oqcsot326w UNIQUE (command_params_id);


--
-- Name: port uk_24jj2vjdky6xs94w12oo3hh6j; Type: CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY port
    ADD CONSTRAINT uk_24jj2vjdky6xs94w12oo3hh6j UNIQUE (urn);


--
-- Name: archived uk_38o878c61aq62m4eem21at38i; Type: CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY archived
    ADD CONSTRAINT uk_38o878c61aq62m4eem21at38i UNIQUE (connection_id);


--
-- Name: vlan_pipe_zaero uk_44qu24pr5pvhv7g9bnsxponew; Type: CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY vlan_pipe_zaero
    ADD CONSTRAINT uk_44qu24pr5pvhv7g9bnsxponew UNIQUE (zaero_id);


--
-- Name: components_fixtures uk_4dyfovp8rupkwogdg7gio956m; Type: CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY components_fixtures
    ADD CONSTRAINT uk_4dyfovp8rupkwogdg7gio956m UNIQUE (fixtures_id);


--
-- Name: vlan_fixture_command_params uk_5aebfi7nf3ltae00w534l0gap; Type: CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY vlan_fixture_command_params
    ADD CONSTRAINT uk_5aebfi7nf3ltae00w534l0gap UNIQUE (command_params_id);


--
-- Name: device_ports uk_7l2uyp0mg028hq3wqdw8adul; Type: CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY device_ports
    ADD CONSTRAINT uk_7l2uyp0mg028hq3wqdw8adul UNIQUE (ports_id);


--
-- Name: held uk_7spoyuxj7tcsrv688tms9eejs; Type: CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY held
    ADD CONSTRAINT uk_7spoyuxj7tcsrv688tms9eejs UNIQUE (connection_id);


--
-- Name: tbl_users uk_c190nfu2w5xwvexf9dv08grsq; Type: CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY tbl_users
    ADD CONSTRAINT uk_c190nfu2w5xwvexf9dv08grsq UNIQUE (username);


--
-- Name: device uk_g9hp5d15ay6ed7fu5c2qs44tx; Type: CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY device
    ADD CONSTRAINT uk_g9hp5d15ay6ed7fu5c2qs44tx UNIQUE (urn);


--
-- Name: vlan_pipe_azero uk_gmpohujxsh7l3vnpvb3krptvw; Type: CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY vlan_pipe_azero
    ADD CONSTRAINT uk_gmpohujxsh7l3vnpvb3krptvw UNIQUE (azero_id);


--
-- Name: design uk_hjp1anpuuo5cqlm97t6bljnfp; Type: CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY design
    ADD CONSTRAINT uk_hjp1anpuuo5cqlm97t6bljnfp UNIQUE (design_id);


--
-- Name: connection uk_iex1ckuqxgik6axvrxdggimly; Type: CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY connection
    ADD CONSTRAINT uk_iex1ckuqxgik6axvrxdggimly UNIQUE (connection_id);


--
-- Name: components_pipes uk_j7d5xcbcfnkf5njkqrsj6nsgx; Type: CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY components_pipes
    ADD CONSTRAINT uk_j7d5xcbcfnkf5njkqrsj6nsgx UNIQUE (pipes_id);


--
-- Name: vlan_junction_command_params uk_jn41a6ds8bv7i0ckdj36vpobp; Type: CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY vlan_junction_command_params
    ADD CONSTRAINT uk_jn41a6ds8bv7i0ckdj36vpobp UNIQUE (command_params_id);


--
-- Name: components_junctions uk_omd8js8gqjtstu3usmqpllei9; Type: CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY components_junctions
    ADD CONSTRAINT uk_omd8js8gqjtstu3usmqpllei9 UNIQUE (junctions_id);


--
-- Name: vlan_fixture_command_params vlan_fixture_command_params_pkey; Type: CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY vlan_fixture_command_params
    ADD CONSTRAINT vlan_fixture_command_params_pkey PRIMARY KEY (vlan_fixture_id, command_params_id);


--
-- Name: vlan_fixture vlan_fixture_pkey; Type: CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY vlan_fixture
    ADD CONSTRAINT vlan_fixture_pkey PRIMARY KEY (id);


--
-- Name: vlan_junction_command_params vlan_junction_command_params_pkey; Type: CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY vlan_junction_command_params
    ADD CONSTRAINT vlan_junction_command_params_pkey PRIMARY KEY (vlan_junction_id, command_params_id);


--
-- Name: vlan_junction vlan_junction_pkey; Type: CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY vlan_junction
    ADD CONSTRAINT vlan_junction_pkey PRIMARY KEY (id);


--
-- Name: vlan_pipe_command_params vlan_pipe_command_params_pkey; Type: CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY vlan_pipe_command_params
    ADD CONSTRAINT vlan_pipe_command_params_pkey PRIMARY KEY (vlan_pipe_id, command_params_id);


--
-- Name: vlan_pipe vlan_pipe_pkey; Type: CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY vlan_pipe
    ADD CONSTRAINT vlan_pipe_pkey PRIMARY KEY (id);


--
-- Name: vlan vlan_pkey; Type: CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY vlan
    ADD CONSTRAINT vlan_pkey PRIMARY KEY (id);


--
-- Name: port_adjcy fk1drhy0o1jwym51oc76x1valv4; Type: FK CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY port_adjcy
    ADD CONSTRAINT fk1drhy0o1jwym51oc76x1valv4 FOREIGN KEY (a_id) REFERENCES port(id);


--
-- Name: connection fk1h8nw0pae7grhqrjyxh9fca2e; Type: FK CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY connection
    ADD CONSTRAINT fk1h8nw0pae7grhqrjyxh9fca2e FOREIGN KEY (archived_id) REFERENCES archived(id);


--
-- Name: vlan_junction_command_params fk24chiux3a5bvfks2qiq940lg3; Type: FK CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY vlan_junction_command_params
    ADD CONSTRAINT fk24chiux3a5bvfks2qiq940lg3 FOREIGN KEY (command_params_id) REFERENCES command_param(id);


--
-- Name: archived fk2g7ghm5uv1ghfuha573akglsp; Type: FK CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY archived
    ADD CONSTRAINT fk2g7ghm5uv1ghfuha573akglsp FOREIGN KEY (cmp_id) REFERENCES components(id);


--
-- Name: vlan_pipe_zaero fk2kc6sph2vyu1w88ssw1a0eck; Type: FK CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY vlan_pipe_zaero
    ADD CONSTRAINT fk2kc6sph2vyu1w88ssw1a0eck FOREIGN KEY (zaero_id) REFERENCES ero_hop(id);


--
-- Name: components_junctions fk2rayol4na9us0nfsnkcyly2cu; Type: FK CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY components_junctions
    ADD CONSTRAINT fk2rayol4na9us0nfsnkcyly2cu FOREIGN KEY (junctions_id) REFERENCES vlan_junction(id);


--
-- Name: vlan_fixture fk31veat2doj1bdkqd9a6ugssv4; Type: FK CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY vlan_fixture
    ADD CONSTRAINT fk31veat2doj1bdkqd9a6ugssv4 FOREIGN KEY (schedule_id) REFERENCES schedule(id);


--
-- Name: vlan_pipe fk34ql0sn662eijeagigwc6u9p0; Type: FK CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY vlan_pipe
    ADD CONSTRAINT fk34ql0sn662eijeagigwc6u9p0 FOREIGN KEY (schedule_id) REFERENCES schedule(id);


--
-- Name: connection fk3sde6t66ka4h0ra2u8mvehh86; Type: FK CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY connection
    ADD CONSTRAINT fk3sde6t66ka4h0ra2u8mvehh86 FOREIGN KEY (reserved_id) REFERENCES reserved(id);


--
-- Name: vlan_pipe_command_params fk4cs1xbjrdpflpku2x1ebfm5gw; Type: FK CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY vlan_pipe_command_params
    ADD CONSTRAINT fk4cs1xbjrdpflpku2x1ebfm5gw FOREIGN KEY (command_params_id) REFERENCES command_param(id);


--
-- Name: components_fixtures fk4heyttwp39c3xil8doj3heytp; Type: FK CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY components_fixtures
    ADD CONSTRAINT fk4heyttwp39c3xil8doj3heytp FOREIGN KEY (fixtures_id) REFERENCES vlan_fixture(id);


--
-- Name: components_fixtures fk4p8a8cnbljjgc1rpxkftkmxsw; Type: FK CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY components_fixtures
    ADD CONSTRAINT fk4p8a8cnbljjgc1rpxkftkmxsw FOREIGN KEY (components_id) REFERENCES components(id);


--
-- Name: vlan_junction fk5e984f06mxt1tduoh5yo7brps; Type: FK CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY vlan_junction
    ADD CONSTRAINT fk5e984f06mxt1tduoh5yo7brps FOREIGN KEY (vlan_id) REFERENCES vlan(id);


--
-- Name: design fk5td1kh1xy48ili56cpc277th7; Type: FK CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY design
    ADD CONSTRAINT fk5td1kh1xy48ili56cpc277th7 FOREIGN KEY (cmp_id) REFERENCES components(id);


--
-- Name: vlan_fixture_command_params fk6fl6uwrsngc6dk2ltsxrkihwh; Type: FK CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY vlan_fixture_command_params
    ADD CONSTRAINT fk6fl6uwrsngc6dk2ltsxrkihwh FOREIGN KEY (command_params_id) REFERENCES command_param(id);


--
-- Name: vlan_pipe_azero fk6i983sjud5uullvf10fo3gjdq; Type: FK CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY vlan_pipe_azero
    ADD CONSTRAINT fk6i983sjud5uullvf10fo3gjdq FOREIGN KEY (azero_id) REFERENCES ero_hop(id);


--
-- Name: vlan_pipe_command_params fk6icfqvyd3l382mshwvekqjluu; Type: FK CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY vlan_pipe_command_params
    ADD CONSTRAINT fk6icfqvyd3l382mshwvekqjluu FOREIGN KEY (vlan_pipe_id) REFERENCES vlan_pipe(id);


--
-- Name: connection fk6w3a60c5pj9qfh9cdk1roppnq; Type: FK CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY connection
    ADD CONSTRAINT fk6w3a60c5pj9qfh9cdk1roppnq FOREIGN KEY (held_id) REFERENCES held(id);


--
-- Name: reserved fk8d3eiu18bkw1b0q0n87001yif; Type: FK CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY reserved
    ADD CONSTRAINT fk8d3eiu18bkw1b0q0n87001yif FOREIGN KEY (cmp_id) REFERENCES components(id);


--
-- Name: reserved fk8rv5huaug8p1yj57skmd4r4bn; Type: FK CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY reserved
    ADD CONSTRAINT fk8rv5huaug8p1yj57skmd4r4bn FOREIGN KEY (schedule_id) REFERENCES schedule(id);


--
-- Name: vlan fk9f94ucmhvfj0n72f057eikfhh; Type: FK CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY vlan
    ADD CONSTRAINT fk9f94ucmhvfj0n72f057eikfhh FOREIGN KEY (schedule_id) REFERENCES schedule(id);


--
-- Name: vlan_pipe fkbnyr81yanlbdl4ge02o1gaopj; Type: FK CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY vlan_pipe
    ADD CONSTRAINT fkbnyr81yanlbdl4ge02o1gaopj FOREIGN KEY (z_id) REFERENCES vlan_junction(id);


--
-- Name: device_reservable_vlans fkdlvowg0jd6b790ja3cryv9rtb; Type: FK CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY device_reservable_vlans
    ADD CONSTRAINT fkdlvowg0jd6b790ja3cryv9rtb FOREIGN KEY (device_id) REFERENCES device(id);


--
-- Name: components_pipes fkf6tbe45puskk4mcvs09ym8hxr; Type: FK CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY components_pipes
    ADD CONSTRAINT fkf6tbe45puskk4mcvs09ym8hxr FOREIGN KEY (components_id) REFERENCES components(id);


--
-- Name: command_param fkfnwifol3ch5oosbtnudf8nn0v; Type: FK CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY command_param
    ADD CONSTRAINT fkfnwifol3ch5oosbtnudf8nn0v FOREIGN KEY (schedule_id) REFERENCES schedule(id);


--
-- Name: vlan_fixture fkg8816dvwfbimv96eqin085jr5; Type: FK CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY vlan_fixture
    ADD CONSTRAINT fkg8816dvwfbimv96eqin085jr5 FOREIGN KEY (junction_id) REFERENCES vlan_junction(id);


--
-- Name: vlan_fixture_command_params fkgaqm8qjp1j9sgfl6f6siq2ysp; Type: FK CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY vlan_fixture_command_params
    ADD CONSTRAINT fkgaqm8qjp1j9sgfl6f6siq2ysp FOREIGN KEY (vlan_fixture_id) REFERENCES vlan_fixture(id);


--
-- Name: port_reservable_vlans fkgbt4ulvh9vectjga9uom6d17; Type: FK CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY port_reservable_vlans
    ADD CONSTRAINT fkgbt4ulvh9vectjga9uom6d17 FOREIGN KEY (port_id) REFERENCES port(id);


--
-- Name: vlan_pipe fkggx2fnq99qjc7a78ofhi0ynyh; Type: FK CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY vlan_pipe
    ADD CONSTRAINT fkggx2fnq99qjc7a78ofhi0ynyh FOREIGN KEY (a_id) REFERENCES vlan_junction(id);


--
-- Name: port_adjcy_metrics fkgxbxa59t12nuvq3iw8h2m1wxj; Type: FK CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY port_adjcy_metrics
    ADD CONSTRAINT fkgxbxa59t12nuvq3iw8h2m1wxj FOREIGN KEY (port_adjcy_id) REFERENCES port_adjcy(id);


--
-- Name: device_capabilities fkhl5k4w984c88p08xgoh1h7891; Type: FK CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY device_capabilities
    ADD CONSTRAINT fkhl5k4w984c88p08xgoh1h7891 FOREIGN KEY (device_id) REFERENCES device(id);


--
-- Name: port_capabilities fkia6tkai22vmyt7d9iimfv6c6; Type: FK CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY port_capabilities
    ADD CONSTRAINT fkia6tkai22vmyt7d9iimfv6c6 FOREIGN KEY (port_id) REFERENCES port(id);


--
-- Name: device_ports fkj54bcd7wi9st57a0il3n3i8c8; Type: FK CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY device_ports
    ADD CONSTRAINT fkj54bcd7wi9st57a0il3n3i8c8 FOREIGN KEY (ports_id) REFERENCES port(id);


--
-- Name: components_pipes fkjak099jqd9v3eaipjc9kjtenm; Type: FK CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY components_pipes
    ADD CONSTRAINT fkjak099jqd9v3eaipjc9kjtenm FOREIGN KEY (pipes_id) REFERENCES vlan_pipe(id);


--
-- Name: port_adjcy fkjc75uo2tyqe5l4b5itl39bf64; Type: FK CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY port_adjcy
    ADD CONSTRAINT fkjc75uo2tyqe5l4b5itl39bf64 FOREIGN KEY (z_id) REFERENCES port(id);


--
-- Name: vlan_pipe_azero fkk3oc37kkx8b0987765v8wkf23; Type: FK CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY vlan_pipe_azero
    ADD CONSTRAINT fkk3oc37kkx8b0987765v8wkf23 FOREIGN KEY (vlan_pipe_id) REFERENCES vlan_pipe(id);


--
-- Name: port fkkf8semrjdomxgnhbq6l979chu; Type: FK CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY port
    ADD CONSTRAINT fkkf8semrjdomxgnhbq6l979chu FOREIGN KEY (device_id) REFERENCES device(id);


--
-- Name: vlan_fixture fkkqqlvdf9dpawflyiivxddpc37; Type: FK CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY vlan_fixture
    ADD CONSTRAINT fkkqqlvdf9dpawflyiivxddpc37 FOREIGN KEY (vlan_id) REFERENCES vlan(id);


--
-- Name: vlan_junction_command_params fkq4kxrlx0ka6745gd14y8ely5p; Type: FK CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY vlan_junction_command_params
    ADD CONSTRAINT fkq4kxrlx0ka6745gd14y8ely5p FOREIGN KEY (vlan_junction_id) REFERENCES vlan_junction(id);


--
-- Name: held fkq4ma47h182wtt2ls6ai56rawn; Type: FK CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY held
    ADD CONSTRAINT fkq4ma47h182wtt2ls6ai56rawn FOREIGN KEY (schedule_id) REFERENCES schedule(id);


--
-- Name: vlan_junction fkq7t2fexxue2v5oham8qv5hme6; Type: FK CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY vlan_junction
    ADD CONSTRAINT fkq7t2fexxue2v5oham8qv5hme6 FOREIGN KEY (schedule_id) REFERENCES schedule(id);


--
-- Name: device_ports fkqtqfgllr6rhqv0v5wwbmb95oy; Type: FK CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY device_ports
    ADD CONSTRAINT fkqtqfgllr6rhqv0v5wwbmb95oy FOREIGN KEY (device_id) REFERENCES device(id);


--
-- Name: archived fksakqty1t4o3l67nac1d643pje; Type: FK CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY archived
    ADD CONSTRAINT fksakqty1t4o3l67nac1d643pje FOREIGN KEY (schedule_id) REFERENCES schedule(id);


--
-- Name: components_junctions fkt2wk613iftnwb8y2ic87rkm6p; Type: FK CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY components_junctions
    ADD CONSTRAINT fkt2wk613iftnwb8y2ic87rkm6p FOREIGN KEY (components_id) REFERENCES components(id);


--
-- Name: held fktd5836o6y6we6ty8dtju5pc71; Type: FK CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY held
    ADD CONSTRAINT fktd5836o6y6we6ty8dtju5pc71 FOREIGN KEY (cmp_id) REFERENCES components(id);


--
-- Name: vlan_pipe_zaero fkx2r9h5yd7lq2n3ef6e563gg8; Type: FK CONSTRAINT; Schema: public; Owner: oscars
--

ALTER TABLE ONLY vlan_pipe_zaero
    ADD CONSTRAINT fkx2r9h5yd7lq2n3ef6e563gg8 FOREIGN KEY (vlan_pipe_id) REFERENCES vlan_pipe(id);


--
-- PostgreSQL database dump complete
--

