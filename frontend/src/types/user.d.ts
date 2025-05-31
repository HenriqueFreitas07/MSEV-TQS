export type User = {
  id: string;
  name: string;
  email: string;
  operator: boolean;
}

export type SignupDTO = {
  name: string;
  email: string;
  password: string;
}

export type LoginDTO = Omit<SignupDTO, "name">;
