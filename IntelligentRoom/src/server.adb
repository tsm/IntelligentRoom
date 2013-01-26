with Ada.Text_IO; use Ada.Text_IO;
with AWS;
with AWS.Default;
with AWS.Server;
with AWS.Response;
with AWS.Status;
with Ada.Strings.Hash;
with Ada.Containers.Hashed_Maps; use Ada.Containers;
with Ada.Strings.Unbounded; use Ada.Strings.Unbounded;
with AWS.Parameters;
with AWS.Messages;
with AWS.MIME;

-----------------------------------------------
-- Authors: Czarnik Tomasz, Smilek Krzysztof --
-----------------------------------------------

procedure Server is

   ----------------------------- DEFINICJA TYPOW  -----------------------------
   subtype Hour is Integer range 0 .. 23;
   subtype Minute is Integer range 0 .. 59;

   -- Czas (HH:MM)
   type Time is record
      t_hour	: Hour;
      t_minute 	: Minute;
   end record;

   function "<" (time1,time2 : IN Time) return Boolean IS
   begin
      IF time1.t_hour < time2.t_hour THEN
         return True;
      ELSIF time1.t_hour = time2.t_hour AND time1.t_minute < time2.t_minute THEN
         return True;
      ELSE
         return False;
      END IF;
   end "<";

   -- Konfiguracja Klienta
   type Client_Configuration is record
      name 		: Unbounded_String;  	-- nazwa klienta
      active_from	: Time; 		-- aktywnosc od
      active_to		: Time;			-- aktywnosc do
      brightness	: Integer;		-- ustawienie jasnosci klienta
      active_now	: Boolean;		-- czy aktywny teraz
      force		: Boolean;		-- czy jest wymuszenie
   end record;

   -- Funkcja hashujaca mape
   function Hash_Func(Key : Unbounded_String) return Ada.Containers.Hash_Type is
   begin
      return Ada.Strings.Hash(To_String(Key));
   end Hash_Func;

   -- Mapa przechowujaca klientow i ich konfiguracje
   package Clients is new Ada.Containers.Hashed_Maps
     (Key_Type => Unbounded_String,
      Element_Type => Client_Configuration,
      Hash => Hash_Func,
      Equivalent_Keys => "=");

   ----------------------------- DEFINICJA ZMIENNYCH ---------------------------
   TheServer : AWS.Server.HTTP;
   clientsMap : Clients.Map;

   ----------------------------- Server_CallBack -------------------------------

   function CallBack (Request : AWS.Status.Data) return AWS.Response.Data is
      URI : constant String := AWS.Status.URI (Request);
      Params : constant AWS.Parameters.List := AWS.Status.Parameters (Request);
   begin
      Put_Line (URI);
      IF URI = "/" then
         begin
            return AWS.Response.File (AWS.MIME.Text_HTML, "index.html");
         end;
      elsif URI = "/config" then
         declare
            client : constant String:= AWS.Parameters.Get (Params, "client");
            a_from_h : constant String := AWS.Parameters.Get (Params, "a_from_h");
            a_from_m : constant String := AWS.Parameters.Get (Params, "a_from_m");
            a_to_h : constant String := AWS.Parameters.Get (Params, "a_to_h");
            a_to_m : constant String := AWS.Parameters.Get (Params, "a_to_m");
            def_value : constant String := AWS.Parameters.Get (Params, "def_value");
            a_f_h,a_t_h : Hour;
            a_f_m,a_t_m : Minute;
            active_from,active_to : Time;
            def_val : Integer;
         begin
            if clientsMap.contains(To_Unbounded_String(client)) then
               return AWS.Response.Build (AWS.MIME.Text_Plain, "Podany klient juz istnieje!");
            else
               -- przpyspisanie wartosci
               if a_from_h = "" then a_f_h :=Positive'Value("0");else a_f_h:=Positive'Value(a_from_h); end if;
               if a_from_m = "" then a_f_m :=Positive'Value("0");else a_f_m:=Positive'Value(a_from_m); end if;
               if a_to_h = "" then a_t_h :=Positive'Value("0");else a_t_h:=Positive'Value(a_to_h); end if;
               if a_to_m = "" then a_t_m :=Positive'Value("0");else a_t_m:=Positive'Value(a_to_m); end if;
               if def_value = "" then def_val := 350; else def_val := Positive'Value(def_value); end if;
               active_from := (a_f_h,a_f_m);
               active_to := (a_t_h,a_t_m);

               -- dodawanie klientow do mapy
               clientsMap.Insert(To_Unbounded_String(client),(To_Unbounded_String(client),active_from,active_to,def_val,False,False) );

               return AWS.Response.Build (AWS.MIME.Text_Plain, "OK");
            end if;
         end;
      elsif URI = "/read" then
         declare
            client : constant String := AWS.Parameters.Get (Params, "client");
            config : Client_Configuration;
         begin
            if clientsMap.contains(To_Unbounded_String(client)) then
               config := clientsMap.Element(To_Unbounded_String(client));
               return AWS.Response.Build ("text/html", "<p>Klient: "&config.name&"<br>"
                                       &"Aktywne od: "&Hour'Image(config.active_from.t_hour)&" :"&Minute'Image(config.active_from.t_minute)&"<br>"
                                       &"Aktywne do: "&Hour'Image(config.active_to.t_hour)&" :"&Minute'Image(config.active_to.t_minute)&"<br>"
                                       &"Jasnosc: "&Integer'Image(config.brightness)&"<br>"
                                          &"Aktywne: "&Boolean'Image(config.active_now));
            else
               return AWS.Response.Build (AWS.MIME.Text_Plain, "Nie znaleziono klienta!");
            end if;
         end;
      elsif URI = "/force" then
          declare
            client : constant String := AWS.Parameters.Get (Params, "client");
            value : constant String := AWS.Parameters.Get (Params, "value");
            config : Client_Configuration;
         begin
            if clientsMap.contains(To_Unbounded_String(client)) then
               config := clientsMap.Element(To_Unbounded_String(client));
               if value = "1" then config.active_now := True; else config.active_now := False; end if;
               clientsMap.replace(To_Unbounded_String(client),config);
               return AWS.Response.Build (AWS.MIME.Text_Plain, "OK");
            else
               return AWS.Response.Build (AWS.MIME.Text_Plain, "Nie znaleziono klienta!");
            end if;

         end;
      elsif URI = "/setLight" then
         declare
            client : constant String := AWS.Parameters.Get (Params, "client");
            value : constant String := AWS.Parameters.Get (Params, "value");
            t_h : constant String := AWS.Parameters.Get (Params, "t_h");
            t_m : constant String := AWS.Parameters.Get (Params, "t_m");
            config : Client_Configuration;
            now : Time;
            brightness, result : Integer;
            temp,t1,t2 : Float;
         begin
            if clientsMap.contains(To_Unbounded_String(client)) then
               --odczytanie parametrow
               now := (Hour'Value(t_h),Minute'Value(t_m));
               brightness := Positive'Value(value);
               config := clientsMap.Element(To_Unbounded_String(client));
               -- update zmiennej active_now jesli natrafiono na przedzial czasowy
               IF config.active_from < config.active_to THEN
                  IF now = config.active_from OR (config.active_from < now AND now < config.active_to) THEN
                     config.active_now := True;
                  ELSE
                     config.active_now := False;
                  END IF;
               ELSIF config.active_from = config.active_to THEN
                  config.active_now := False;
               ELSE
                  IF now = config.active_from
                    OR ((config.active_from < now AND now < (23,59)) OR now = (23,59))
                    OR (now = (0,0) OR ((0,0) < now AND now < config.active_to)) THEN
                     config.active_now := True;
                  ELSE
                     config.active_now := False;
                  END IF;
               END IF;
               clientsMap.replace(To_Unbounded_String(client),config);

               -- warunek sterowania oswietleniem
               IF config.active_now OR config.force THEN
                  t1 := float(brightness);
               	  t2 := float(config.brightness);
                  IF t1 >= t2-50.0 THEN
                     temp := 0.0;
                  ELSE
                     temp := (1.0 - (t1 / t2)) * 100.0;
                  END IF;
                  result := Integer(temp);
                  return AWS.Response.Build (AWS.MIME.Text_Plain, Integer'Image(result));
               ELSE
                  return AWS.Response.Build (AWS.MIME.Text_Plain, "Sterowanie oswietleniem jest nieaktywne!");
               END IF;
            else
               return AWS.Response.Build (AWS.MIME.Text_Plain, "Nie znaleziono klienta!");
            end if;
         end;
      else
         return AWS.Response.Acknowledge(Status_Code => AWS.Messages.S404,
                                     Message_Body => "404 Not found",
                                     Content_Type => AWS.MIME.Text_Plain);
      end if;

   end CallBack;

    ----------------------------- KOD SERWERA  ---------------------------------
begin
   AWS.Server.Start(TheServer, "IntelligentRoomServer",
                    Max_Connection => 30,
                    Port => 8181,
                    Callback => CallBack'Unrestricted_Access);
   Put_Line
     ("Server active : http://localhost:8181/");
   Put_Line ("Press 'Q' key to quit.");
   AWS.Server.Wait (AWS.Server.Q_Key_Pressed);
end Server;
