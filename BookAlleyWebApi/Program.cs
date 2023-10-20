using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using BookAlleyWebApi.Models;

namespace BookAlleyWebApi
{
    public class Program
    {
        public static void Main(string[] args)
        {
            var builder = WebApplication.CreateBuilder(args);

            // Add services to the container.
            builder.Services.AddControllers();
            var connection = ConfigurationExtensions.GetConnectionString(builder.Configuration, "AzureConnectionString");
            Console.WriteLine($"Connection String: {connection}");
            builder.Services.AddDbContext<BookAlleyContext>(options =>
                options.UseSqlServer(connection));

            // Learn more about configuring Swagger/OpenAPI at https://aka.ms/aspnetcore/swashbuckle
            builder.Services.AddEndpointsApiExplorer();
            builder.Services.AddSwaggerGen();

            var app = builder.Build();

            // Configure the HTTP request pipeline.
            if (app.Environment.IsDevelopment())
            {
                app.UseSwagger();
                app.UseSwaggerUI();
            }

            app.UseHttpsRedirection();

            app.UseAuthorization();


            app.MapControllers();

            app.Run();
        }
    }
}