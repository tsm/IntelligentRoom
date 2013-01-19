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

procedure Server is

   ----------------------------- DEFINICJA TYPOW  -----------------------------
   subtype Hour is Integer range 0 .. 23;
   subtype Minute is Integer range 0 .. 59;

   type Time is record
      t_hour	: Hour;
      t_minute 	: Minute;
   end record;

   -- Konfiguracja Klienta
   type Client_Configuration is record
      name 		: Unbounded_String;
      active_from	: Time;
      active_to		: Time;
      active_now	: Boolean;
   end record;

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
      IF URI = "/config" then
         declare
            client : constant String:= AWS.Parameters.Get (Params, "client");
            a_from_h : constant String := AWS.Parameters.Get (Params, "a_from_h");
            a_from_m : constant String := AWS.Parameters.Get (Params, "a_from_m");
            a_to_h : constant String := AWS.Parameters.Get (Params, "a_to_h");
            a_to_m : constant String := AWS.Parameters.Get (Params, "a_to_m");
            a_force : constant String := AWS.Parameters.Get (Params, "a_force");
            a_f_h,a_t_h : Hour;
            a_f_m,a_t_m : Minute;
            active_from,active_to : Time;
            active : Boolean := False;
         begin
            if a_from_h = "" then a_f_h :=Positive'Value("0");else a_f_h:=Positive'Value(a_from_h); end if;
            if a_from_m = "" then a_f_m :=Positive'Value("0");else a_f_m:=Positive'Value(a_from_m); end if;
            if a_to_h = "" then a_t_h :=Positive'Value("0");else a_t_h:=Positive'Value(a_to_h); end if;
            if a_to_m = "" then a_t_m :=Positive'Value("0");else a_t_m:=Positive'Value(a_to_m); end if;
            if a_force = "1" then active := True; else active := False; end if;
            active_from := (a_f_h,a_f_m);
            active_to := (a_t_h,a_t_m);


            --dorobic parsowanie czasu
            Put_Line ("Klient:"&client&" from:"&a_from_h&", to:"&a_to_h&" force"&a_force);

            clientsMap.Insert(To_Unbounded_String(client),(To_Unbounded_String(client),active_from,active_to,active) );

            return AWS.Response.Build ("text/html", "<p>Klient:"&client&"<br>"
                                       &"Aktywne od: "&a_from_h&":"&a_from_m&"<br>"
                                       &"Aktywne do: "&a_to_h&":"&a_to_m&"<br>"
                                       &"Wymuszenie: "&a_force);
         end;
      elsif URI = "/read" then
         declare
            client : constant String := AWS.Parameters.Get (Params, "client");
            config : Client_Configuration;
         begin
            config := clientsMap.Element(To_Unbounded_String(client));
          --  if config then
          --     return AWS.Response.Build ("text/html", "<p>Nie znaleziono klienta!");
          --  else
               return AWS.Response.Build ("text/html", "<p>Klient:"&config.name&"<br>"
                                       &"Aktywne od: "&Integer'Image(config.active_from.t_hour)&":"&Integer'Image(config.active_from.t_minute)&"<br>"
                                       &"Aktywne do: "&Integer'Image(config.active_to.t_hour)&":"&Integer'Image(config.active_to.t_minute)&"<br>"
                                          &"Wymuszenie: "&Boolean'Image(config.active_now));
          --  end if;
         end;
      else
         return AWS.Response.Build ("text/html", "<p>Hello world !");
      end if;

   end CallBack;

begin
   Put_Line
     ("Server active : http://localhost:8181/");

   AWS.Server.Start(TheServer, "IntelligentRoomServer",
                    Max_Connection => 30,
                    Port => 8181,
                    Callback => CallBack'Unrestricted_Access);
   Put_Line ("Press 'Q' key to quit.");
   AWS.Server.Wait (AWS.Server.Q_Key_Pressed);

end Server;
